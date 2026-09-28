import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import mongoose from 'mongoose';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { sendParcelNotification } from './mailer.js';

const app = express();
const port = process.env.PORT || 5000;
const jwtSecret = process.env.JWT_SECRET || 'local-development-secret-change-me';

app.use(cors({ origin: process.env.CLIENT_ORIGIN || 'http://localhost:5173' }));
app.use(express.json());

const userSchema = new mongoose.Schema({
  username: { type: String, required: true, unique: true, trim: true },
  password: { type: String, required: true },
  role: { type: String, enum: ['ADMIN', 'SECURITY', 'STUDENT'], required: true },
  enabled: { type: Boolean, default: true }
}, { timestamps: true });

const studentSchema = new mongoose.Schema({
  registerNumber: { type: String, required: true, unique: true, trim: true },
  name: { type: String, required: true, trim: true },
  department: { type: String, required: true, trim: true },
  year: { type: Number, required: true },
  phone: { type: String, required: true, trim: true },
  email: { type: String, required: true, unique: true, lowercase: true, trim: true }
}, { timestamps: true });

const parcelSchema = new mongoose.Schema({
  parcelId: { type: String, required: true, unique: true, trim: true },
  student: { type: mongoose.Schema.Types.ObjectId, ref: 'Student', required: true },
  courierName: { type: String, required: true, trim: true },
  senderName: { type: String, trim: true, default: '' },
  receivedDate: { type: String, required: true },
  storageLocation: { type: String, trim: true, default: '' },
  status: { type: String, enum: ['RECEIVED', 'READY_FOR_COLLECTION', 'COLLECTED'], default: 'RECEIVED' },
  collectedDate: { type: String, default: null }
}, { timestamps: true });

const User = mongoose.model('User', userSchema);
const Student = mongoose.model('Student', studentSchema);
const Parcel = mongoose.model('Parcel', parcelSchema);

const toStudentResponse = student => ({
  id: student._id,
  registerNumber: student.registerNumber,
  name: student.name,
  department: student.department,
  year: student.year,
  phone: student.phone,
  email: student.email
});

const toParcelResponse = parcel => ({
  id: parcel._id,
  parcelId: parcel.parcelId,
  studentRegisterNumber: parcel.student?.registerNumber,
  studentName: parcel.student?.name,
  courierName: parcel.courierName,
  senderName: parcel.senderName,
  receivedDate: parcel.receivedDate,
  storageLocation: parcel.storageLocation,
  status: parcel.status,
  collectedDate: parcel.collectedDate
});

function signToken(user) {
  return jwt.sign({ id: user._id.toString(), username: user.username, role: user.role }, jwtSecret, { expiresIn: process.env.JWT_EXPIRES_IN || '1d' });
}

function auth(req, res, next) {
  const header = req.headers.authorization;
  if (!header?.startsWith('Bearer ')) return res.status(401).json({ message: 'Authentication required' });
  try {
    req.user = jwt.verify(header.slice(7), jwtSecret);
    next();
  } catch {
    return res.status(401).json({ message: 'Invalid or expired token' });
  }
}

function roles(...allowed) {
  return (req, res, next) => allowed.includes(req.user.role)
    ? next()
    : res.status(403).json({ message: 'You do not have permission for this action' });
}

function asyncRoute(handler) {
  return (req, res, next) => Promise.resolve(handler(req, res, next)).catch(next);
}

app.get('/api/health', (req, res) => res.json({ status: 'ok' }));

app.post('/api/auth/login', asyncRoute(async (req, res) => {
  const { username, password } = req.body;
  const user = await User.findOne({ username: username?.trim() });
  if (!user || !user.enabled || !(await bcrypt.compare(password || '', user.password))) {
    return res.status(401).json({ message: 'Invalid username or password' });
  }
  return res.json({ token: signToken(user), username: user.username, role: `ROLE_${user.role}` });
}));

app.get('/api/dashboard', auth, roles('ADMIN', 'SECURITY'), asyncRoute(async (req, res) => {
  const start = new Date().toISOString().slice(0, 10);
  const [totalParcels, received, readyForCollection, collected, receivedToday, collectedToday] = await Promise.all([
    Parcel.countDocuments(),
    Parcel.countDocuments({ status: 'RECEIVED' }),
    Parcel.countDocuments({ status: 'READY_FOR_COLLECTION' }),
    Parcel.countDocuments({ status: 'COLLECTED' }),
    Parcel.countDocuments({ receivedDate: start }),
    Parcel.countDocuments({ collectedDate: start })
  ]);
  res.json({ totalParcels, received, readyForCollection, collected, receivedToday, collectedToday });
}));

app.get('/api/students', auth, roles('ADMIN', 'SECURITY'), asyncRoute(async (req, res) => {
  const students = await Student.find().sort({ name: 1 });
  res.json(students.map(toStudentResponse));
}));

app.get('/api/students/:registerNumber', auth, roles('ADMIN', 'SECURITY'), asyncRoute(async (req, res) => {
  const student = await Student.findOne({ registerNumber: req.params.registerNumber });
  if (!student) return res.status(404).json({ message: 'Student not found' });
  res.json(toStudentResponse(student));
}));

app.post('/api/students', auth, roles('ADMIN'), asyncRoute(async (req, res) => {
  const { registerNumber, name, department, year, phone, email, password } = req.body;
  if (!registerNumber || !name || !department || !year || !phone || !email || !password) return res.status(400).json({ message: 'All student fields are required' });
  if (await Student.exists({ $or: [{ registerNumber }, { email }] })) return res.status(400).json({ message: 'Register number or email already exists' });
  if (await User.exists({ username: registerNumber })) return res.status(400).json({ message: 'User account already exists' });
  const student = await Student.create({ registerNumber, name, department, year, phone, email });
  try {
    await User.create({ username: registerNumber, password: await bcrypt.hash(password, 12), role: 'STUDENT' });
  } catch (error) {
    await Student.findByIdAndDelete(student._id);
    throw error;
  }
  res.status(201).json(toStudentResponse(student));
}));

app.get('/api/parcels/student/:registerNumber', auth, roles('STUDENT'), asyncRoute(async (req, res) => {
  if (req.user.username !== req.params.registerNumber) return res.status(403).json({ message: 'You can only view your own parcels' });
  const studentIds = await Student.find({ registerNumber: req.params.registerNumber }).distinct('_id');
  const parcels = await Parcel.find({ student: { $in: studentIds } }).populate('student');
  res.json(parcels.map(toParcelResponse));
}));

app.get('/api/parcels', auth, roles('ADMIN', 'SECURITY'), asyncRoute(async (req, res) => {
  const parcels = await Parcel.find().populate('student').sort({ receivedDate: -1, createdAt: -1 });
  res.json(parcels.map(toParcelResponse));
}));

app.get('/api/parcels/search', auth, roles('ADMIN', 'SECURITY'), asyncRoute(async (req, res) => {
  const parcel = await Parcel.findOne({ parcelId: req.query.parcelId }).populate('student');
  if (!parcel) return res.status(404).json({ message: 'Parcel not found' });
  res.json(toParcelResponse(parcel));
}));

app.get('/api/parcels/status/:status', auth, roles('ADMIN', 'SECURITY'), asyncRoute(async (req, res) => {
  const parcels = await Parcel.find({ status: req.params.status }).populate('student');
  res.json(parcels.map(toParcelResponse));
}));

app.get('/api/parcels/:id', auth, roles('ADMIN', 'SECURITY'), asyncRoute(async (req, res) => {
  const parcel = await Parcel.findById(req.params.id).populate('student');
  if (!parcel) return res.status(404).json({ message: 'Parcel not found' });
  res.json(toParcelResponse(parcel));
}));

app.post('/api/parcels', auth, roles('ADMIN', 'SECURITY'), asyncRoute(async (req, res) => {
  const { parcelId, studentRegisterNumber, courierName, senderName, receivedDate, storageLocation } = req.body;
  if (!parcelId || !studentRegisterNumber || !courierName || !receivedDate) return res.status(400).json({ message: 'Parcel ID, student, courier, and received date are required' });
  if (await Parcel.exists({ parcelId })) return res.status(400).json({ message: 'Parcel ID already exists' });
  const student = await Student.findOne({ registerNumber: studentRegisterNumber });
  if (!student) return res.status(404).json({ message: 'Student not found' });
  const parcel = await Parcel.create({ parcelId, student: student._id, courierName, senderName, receivedDate, storageLocation });
  await parcel.populate('student');
  sendParcelNotification({ studentName: student.name, studentEmail: student.email, parcelId, courierName, receivedDate, storageLocation }).catch(err => console.error('Email failed:', err.message));
  res.status(201).json(toParcelResponse(parcel));
}));

app.put('/api/parcels/:id', auth, roles('ADMIN', 'SECURITY'), asyncRoute(async (req, res) => {
  const allowed = ['courierName', 'senderName', 'storageLocation', 'status'];
  const updates = Object.fromEntries(Object.entries(req.body).filter(([key, value]) => allowed.includes(key) && value !== null));
  const parcel = await Parcel.findByIdAndUpdate(req.params.id, updates, { new: true, runValidators: true }).populate('student');
  if (!parcel) return res.status(404).json({ message: 'Parcel not found' });
  res.json(toParcelResponse(parcel));
}));

app.post('/api/parcels/:id/collect', auth, roles('ADMIN', 'SECURITY'), asyncRoute(async (req, res) => {
  const parcel = await Parcel.findById(req.params.id);
  if (!parcel) return res.status(404).json({ message: 'Parcel not found' });
  if (parcel.status === 'COLLECTED') return res.status(400).json({ message: 'Parcel has already been collected' });
  parcel.status = 'COLLECTED';
  parcel.collectedDate = new Date().toISOString().slice(0, 10);
  await parcel.save();
  await parcel.populate('student');
  res.json(toParcelResponse(parcel));
}));

app.delete('/api/parcels/:id', auth, roles('ADMIN'), asyncRoute(async (req, res) => {
  const result = await Parcel.findByIdAndDelete(req.params.id);
  if (!result) return res.status(404).json({ message: 'Parcel not found' });
  res.status(204).send();
}));

app.use((error, req, res, next) => {
  if (error?.code === 11000) return res.status(400).json({ message: 'A record with that unique value already exists' });
  console.error(error);
  res.status(500).json({ message: 'Unexpected server error' });
});

async function seedUsers() {
  const defaults = [
    ['admin', 'admin123', 'ADMIN'],
    ['security1', 'security123', 'SECURITY']
  ];
  for (const [username, password, role] of defaults) {
    if (!(await User.exists({ username }))) await User.create({ username, password: await bcrypt.hash(password, 12), role });
  }
}

mongoose.connect(process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/digital_parcel_db')
  .then(async () => {
    await seedUsers();
    app.listen(port, () => console.log(`MERN API listening on http://localhost:${port}`));
  })
  .catch(error => {
    console.error('MongoDB connection failed:', error.message);
    process.exit(1);
  });

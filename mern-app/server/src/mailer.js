import nodemailer from 'nodemailer';

const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS,
  },
});

export async function sendParcelNotification({ studentName, studentEmail, parcelId, courierName, receivedDate, storageLocation }) {
  await transporter.sendMail({
    from: `"Digital Parcel Desk" <${process.env.EMAIL_USER}>`,
    to: studentEmail,
    subject: `Parcel ${parcelId} is ready for collection`,
    html: `
      <p>Hi ${studentName},</p>
      <p>A parcel has been logged for you at the parcel desk.</p>
      <table cellpadding="8" style="border-collapse:collapse;font-family:sans-serif;">
        <tr><td><strong>Parcel ID</strong></td><td>${parcelId}</td></tr>
        <tr><td><strong>Courier</strong></td><td>${courierName}</td></tr>
        <tr><td><strong>Received on</strong></td><td>${receivedDate}</td></tr>
        ${storageLocation ? `<tr><td><strong>Location</strong></td><td>${storageLocation}</td></tr>` : ''}
      </table>
      <p>Please collect it at your earliest convenience.</p>
      <p style="color:#6a7d87;font-size:.85rem;">Digital Parcel Repository — College Operations</p>
    `,
  });
}

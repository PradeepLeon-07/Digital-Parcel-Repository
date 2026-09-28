import FormPanel from './shared/FormPanel';

const FIELDS = [
  ['parcelId', 'Parcel ID'],
  ['studentRegisterNumber', 'Student register number'],
  ['courierName', 'Courier name'],
  ['senderName', 'Sender name'],
  ['receivedDate', 'Received date', 'date'],
  ['storageLocation', 'Storage location'],
];

export default function ParcelForm({ form, setForm, onSubmit }) {
  return <FormPanel title="Record a new parcel" onSubmit={onSubmit} fields={FIELDS} form={form} setForm={setForm} submit="Add parcel" />;
}

import FormPanel from './shared/FormPanel';

const FIELDS = [
  ['registerNumber', 'Register number'],
  ['name', 'Full name'],
  ['department', 'Department'],
  ['year', 'Year', 'number'],
  ['phone', 'Phone'],
  ['email', 'Email', 'email'],
  ['password', 'Temporary password', 'password'],
];

export default function StudentForm({ form, setForm, onSubmit }) {
  return <FormPanel title="Create a student account" onSubmit={onSubmit} fields={FIELDS} form={form} setForm={setForm} submit="Create student" />;
}

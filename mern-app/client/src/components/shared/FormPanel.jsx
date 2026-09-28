import SectionTitle from './SectionTitle';

export default function FormPanel({ title, onSubmit, fields, form, setForm, submit }) {
  return (
    <section className="form-panel">
      <SectionTitle kicker="NEW RECORD" title={title} />
      <form className="form-grid" onSubmit={onSubmit}>
        {fields.map(([key, label, type = 'text']) => (
          <label key={key}>
            {label}
            <input
              required={key !== 'senderName' && key !== 'storageLocation'}
              type={type}
              value={form[key]}
              onChange={e => setForm({ ...form, [key]: e.target.value })}
            />
          </label>
        ))}
        <button className="primary" type="submit">{submit}</button>
      </form>
    </section>
  );
}

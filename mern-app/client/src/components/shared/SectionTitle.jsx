export default function SectionTitle({ kicker, title }) {
  return (
    <div className="section-title">
      <p className="eyebrow">{kicker}</p>
      <h2>{title}</h2>
    </div>
  );
}

import Loading from './shared/Loading';
import SectionTitle from './shared/SectionTitle';

const CARDS = [
  ['totalParcels', 'Total parcels'],
  ['received', 'Received'],
  ['readyForCollection', 'Ready for collection'],
  ['collected', 'Collected'],
  ['receivedToday', 'Received today'],
  ['collectedToday', 'Collected today'],
];

export default function Overview({ data }) {
  if (!data) return <Loading />;
  return (
    <>
      <SectionTitle kicker="OPERATIONS / TODAY" title="A quick read of the desk" />
      <div className="metrics">
        {CARDS.map(([key, label]) => (
          <article key={key}>
            <strong>{data[key]}</strong>
            <span>{label}</span>
          </article>
        ))}
      </div>
    </>
  );
}

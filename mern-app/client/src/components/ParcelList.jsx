import SectionTitle from './shared/SectionTitle';

export default function ParcelList({ parcels, readOnly = false, filter, setFilter, search, setSearch, onRefresh, onReady, onCollect, onDelete, isAdmin }) {
  return (
    <>
      <SectionTitle
        kicker={readOnly ? 'STUDENT VIEW' : 'OPERATIONS / INVENTORY'}
        title={readOnly ? 'Parcels assigned to you' : 'Every parcel in the repository'}
      />
      {!readOnly && (
        <div className="toolbar">
          <input placeholder="Search parcel ID" value={search} onChange={e => setSearch(e.target.value)} />
          <select value={filter} onChange={e => { setFilter(e.target.value); setTimeout(onRefresh, 0); }}>
            <option value="">All statuses</option>
            <option value="RECEIVED">Received</option>
            <option value="READY_FOR_COLLECTION">Ready for collection</option>
            <option value="COLLECTED">Collected</option>
          </select>
          <button className="ghost" onClick={onRefresh}>Refresh</button>
        </div>
      )}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Parcel</th><th>Student</th><th>Courier</th><th>Arrived</th><th>Status</th>
              {!readOnly && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {parcels.map(parcel => (
              <tr key={parcel.id}>
                <td><strong>{parcel.parcelId}</strong><small>{parcel.storageLocation || 'No location'}</small></td>
                <td>{parcel.studentName}<small>{parcel.studentRegisterNumber}</small></td>
                <td>{parcel.courierName}<small>{parcel.senderName || 'Sender not recorded'}</small></td>
                <td>{parcel.receivedDate}<small>{parcel.collectedDate ? `Collected ${parcel.collectedDate}` : ''}</small></td>
                <td><span className={`status ${parcel.status.toLowerCase()}`}>{parcel.status.replaceAll('_', ' ')}</span></td>
                {!readOnly && (
                  <td className="actions">
                    {parcel.status === 'RECEIVED' && <button onClick={() => onReady(parcel.id)}>Mark ready</button>}
                    {parcel.status !== 'COLLECTED' && <button onClick={() => onCollect(parcel.id)}>Collect</button>}
                    {isAdmin && <button className="danger" onClick={() => onDelete(parcel.id)}>Delete</button>}
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
        {!parcels.length && <p className="empty">No parcels found.</p>}
      </div>
    </>
  );
}

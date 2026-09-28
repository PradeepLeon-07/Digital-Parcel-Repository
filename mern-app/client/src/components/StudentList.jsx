import SectionTitle from './shared/SectionTitle';

export default function StudentList({ students }) {
  return (
    <>
      <SectionTitle kicker="DIRECTORY" title="Student accounts" />
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Register number</th><th>Name</th><th>Department</th><th>Year</th><th>Contact</th>
            </tr>
          </thead>
          <tbody>
            {students.map(student => (
              <tr key={student.id}>
                <td><strong>{student.registerNumber}</strong></td>
                <td>{student.name}</td>
                <td>{student.department}</td>
                <td>{student.year}</td>
                <td>{student.email}<small>{student.phone}</small></td>
              </tr>
            ))}
          </tbody>
        </table>
        {!students.length && <p className="empty">No students found.</p>}
      </div>
    </>
  );
}

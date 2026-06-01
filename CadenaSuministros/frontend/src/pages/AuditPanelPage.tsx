import { auditService } from '../api/auditService';
import { PageHeader } from '../components/PageHeader';
import { useDataFetch } from '../hooks/useDataFetch';
import { DataView } from '../components/DataView';

export function AuditPanelPage() {
  const { data: commands, loading, error, refresh } = useDataFetch(
    () => auditService.getCommandHistory()
  );

  return (
    <div>
      <PageHeader title="Panel de Auditoría" subtitle="Historial de comandos ejecutados y operaciones realizadas">
        <button className="btn btn-outline btn-sm" onClick={refresh}>Actualizar</button>
      </PageHeader>

      <div className="card">
        <div className="card-header">
          <span className="card-title">Comandos Ejecutados</span>
          {!loading && commands && (
            <span className="text-xs text-secondary">{commands.length} operaciones</span>
          )}
        </div>

        <DataView
          loading={loading}
          error={error}
          data={commands}
          onRetry={refresh}
          empty={
            <div className="card-body">
              <p className="empty-state-sm">
                No hay comandos registrados. Realiza operaciones en la sección de Envíos para ver el historial.
              </p>
            </div>
          }
        >
          {(cmds) => (
            <div className="overflow-auto">
              <table className="table-enhanced">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Descripción</th>
                    <th>Tipo</th>
                  </tr>
                </thead>
                <tbody>
                  {cmds.map((cmd, i) => {
                    const isUndo = cmd.toLowerCase().includes('undo') || cmd.toLowerCase().includes('reverse');
                    const isStatus = cmd.startsWith('UpdateStatus');
                    const isLocation = cmd.startsWith('UpdateLocation');
                    const isCreate = cmd.startsWith('CreateShipment');
                    return (
                      <tr key={i} className="row-enter">
                        <td className="text-xs text-secondary font-mono">{cmds.length - i}</td>
                        <td className="font-mono text-sm">{cmd}</td>
                        <td>
                          {isCreate ? <span className="badge badge-delivered">Creación</span>
                            : isStatus ? <span className="badge badge-transit">Estado</span>
                            : isLocation ? <span className="badge badge-pending">Ubicación</span>
                            : isUndo ? <span className="badge badge-delayed">Reversión</span>
                            : <span className="badge">-</span>}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </DataView>

        <div style={{ padding: '0.75rem 1rem', borderTop: '1px solid var(--border)', fontSize: '0.8rem', color: 'var(--secondary)' }}>
          Este panel muestra el historial de comandos ejecutados en el backend. Los comandos se almacenan en el Invoker con un máximo de 50 entradas.
        </div>
      </div>
    </div>
  );
}

export default AuditPanelPage;

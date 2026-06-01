import { ReactNode } from 'react';

interface DataViewProps<T> {
  loading: boolean;
  error: string | null;
  data: T | null | undefined;
  onRetry?: () => void;
  skeleton?: ReactNode;
  errorRender?: (error: string, retry: () => void) => ReactNode;
  empty?: ReactNode;
  isEmpty?: (data: T) => boolean;
  children: (data: T) => ReactNode;
}

function DefaultSkeleton() {
  return (
    <div className="card">
      <div className="skeleton" style={{ height: 400 }} />
    </div>
  );
}

function DefaultError({ error, retry }: { error: string; retry?: () => void }) {
  return (
    <div className="card">
      <p style={{ color: 'var(--danger)', marginBottom: '0.75rem' }}>Error: {error}</p>
      {retry && (
        <button className="btn btn-primary" onClick={retry}>
          Reintentar
        </button>
      )}
    </div>
  );
}

function DefaultEmpty() {
  return (
    <div className="card">
      <div className="empty-state">
        <div className="empty-state-icon">📋</div>
        <p className="empty-state-text">No hay datos disponibles.</p>
      </div>
    </div>
  );
}

function DefaultEmptyTable() {
  return (
    <tr>
      <td colSpan={99} className="empty-state-sm">
        Sin datos
      </td>
    </tr>
  );
}

export { DefaultSkeleton, DefaultError, DefaultEmpty, DefaultEmptyTable };

export function DataView<T>({
  loading,
  error,
  data,
  onRetry,
  skeleton,
  errorRender,
  empty,
  isEmpty,
  children,
}: DataViewProps<T>) {
  if (loading && !data) {
    return skeleton ?? <DefaultSkeleton />;
  }

  if (error && !data) {
    return errorRender?.(error, onRetry!) ?? (
      <DefaultError error={error} retry={onRetry} />
    );
  }

  if (!data) return null;

  const isDataEmpty = isEmpty
    ? isEmpty(data)
    : Array.isArray(data)
    ? data.length === 0
    : false;

  if (isDataEmpty) {
    return empty ?? <DefaultEmpty />;
  }

  return children(data);
}

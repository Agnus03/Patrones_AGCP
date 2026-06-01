import { useSort } from '../hooks/useSort';
import type { Column, SortConfig } from '../types';
import { DefaultEmptyTable } from './DataView';

interface SortableTableProps<T> {
  data: T[];
  columns: Column<T>[];
  defaultSort?: keyof T;
  keyExtractor?: (item: T) => string;
  emptyState?: React.ReactNode;
}

function renderSortIcon<T>(key: keyof T, config: SortConfig<T>) {
  if (config.key !== key) return <span className="sort-icon">&#x21D5;</span>;
  return (
    <span className="sort-icon active">
      {config.direction === 'asc' ? '&#x2191;' : '&#x2193;'}
    </span>
  );
}

export function SortableTable<T>({
  data,
  columns,
  defaultSort,
  keyExtractor,
  emptyState,
}: SortableTableProps<T>) {
  const { sorted, sortConfig, handleSort } = useSort<T>(defaultSort);

  return (
    <div className="overflow-auto">
      <table className="table-enhanced">
        <thead>
          <tr>
            {columns.map((col) => (
              <th
                key={String(col.key)}
                onClick={col.sortable ? () => handleSort(col.key) : undefined}
                style={{
                  cursor: col.sortable ? 'pointer' : undefined,
                  textAlign: col.align ?? 'left',
                }}
              >
                {col.label}
                {col.sortable && renderSortIcon(col.key, sortConfig)}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.length === 0 ? (
            emptyState ?? <DefaultEmptyTable />
          ) : (
            sorted(data, sortConfig).map((item, i) => (
              <tr
                key={keyExtractor ? keyExtractor(item) : String(item[columns[0]?.key] ?? i)}
                className="row-enter"
                style={{ animationDelay: `${i * 50}ms` }}
              >
                {columns.map((col) => (
                  <td
                    key={String(col.key)}
                    style={{ textAlign: col.align ?? 'left' }}
                  >
                    {col.render
                      ? col.render(item[col.key], item)
                      : String(item[col.key] ?? '-')}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

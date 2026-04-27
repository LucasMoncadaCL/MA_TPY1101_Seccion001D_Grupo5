export interface Categoria {
  id: number;
  nombre: string;
  descripcion: string | null;
  activa: boolean;
  createdAt: string;
}

export interface CategoriaAssociationSummary {
  categoryId: number;
  implementCount: number;
  canDelete: boolean;
}

export interface CategoriaPayload {
  nombre: string;
  descripcion: string | null;
}

export interface DeactivateConflict {
  code: string;
  message: string;
}


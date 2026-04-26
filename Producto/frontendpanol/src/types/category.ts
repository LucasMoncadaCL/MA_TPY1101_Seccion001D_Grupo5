export interface Categoria {
  id: number;
  nombre: string;
  activa: boolean;
  createdAt: string;
  updatedAt: string | null;
}

export interface CategoriaAssociationSummary {
  categoryId: number;
  implementCount: number;
  canDelete: boolean;
}

export interface CategoriaPayload {
  nombre: string;
}

export interface DeactivateConflict {
  code: string;
  message: string;
}


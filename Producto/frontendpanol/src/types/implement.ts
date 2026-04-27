export interface ImplementCreatePayload {
  name: string;
  category_id: number | null;
  location_id: number;
}

export interface ImplementUpdatePayload {
  name: string;
  category_id: number | null;
  location_id: number;
}

export interface ImplementSummary {
  id: number;
  name: string;
  category: {
    id: number;
    name: string;
    active: boolean;
  } | null;
  location: {
    id: number;
    name: string;
    description: string | null;
  } | null;
}

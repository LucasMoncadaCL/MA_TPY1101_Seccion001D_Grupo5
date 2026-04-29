export interface ImplementCreatePayload {
  name: string;
  category_id: number;
  item_type: "consumable" | "reusable" | "individual";
  location_id: number;
  description: string | null;
  min_stock: number;
  observations: string | null;
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

export interface ImplementDetail {
  id: number;
  name: string;
  description: string | null;
  item_type: "consumable" | "reusable" | "individual" | null;
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
  categoryId: number | null;
  locationId: number | null;
  min_stock: number | null;
  observations: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

import { apiClient } from "./apiClient";
import type { LocationOption } from "../types/location";

export async function fetchLocations(): Promise<LocationOption[]> {
  const response = await apiClient.get<LocationOption[]>("/api/locations");
  return response.data;
}


import { apiClient } from "./apiClient";
import type { LocationOption } from "../types/location";

export async function fetchLocations(): Promise<LocationOption[]> {
  const response = await apiClient.get<LocationOption[]>("/api/locations");
  return response.data;
}

export async function fetchLocationsForManagement(): Promise<LocationOption[]> {
  const response = await apiClient.get<LocationOption[]>("/api/locations/gestion");
  return response.data;
}

export async function createLocation(payload: { name: string; description: string | null }): Promise<LocationOption> {
  const response = await apiClient.post<LocationOption>("/api/locations", payload);
  return response.data;
}

export async function updateLocation(
  id: number,
  payload: { name: string; description: string | null },
): Promise<LocationOption> {
  const response = await apiClient.put<LocationOption>(`/api/locations/${id}`, payload);
  return response.data;
}

export async function setLocationActive(id: number, active: boolean): Promise<LocationOption> {
  const response = await apiClient.patch<LocationOption>(`/api/locations/${id}/active`, null, { params: { active } });
  return response.data;
}


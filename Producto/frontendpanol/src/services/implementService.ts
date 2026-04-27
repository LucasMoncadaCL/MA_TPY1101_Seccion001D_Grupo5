import { apiClient } from "./apiClient";
import type { ImplementCreatePayload, ImplementSummary, ImplementUpdatePayload } from "../types/implement";

export async function fetchImplements(): Promise<ImplementSummary[]> {
  const response = await apiClient.get<ImplementSummary[]>("/api/implements");
  return response.data;
}

export async function createImplement(payload: ImplementCreatePayload): Promise<void> {
  await apiClient.post("/api/implements", payload);
}

export async function updateImplement(
  implementId: number,
  payload: ImplementUpdatePayload,
): Promise<void> {
  await apiClient.put(`/api/implements/${implementId}`, payload);
}

import { apiClient } from "./apiClient";
import type {
  IndividualUpdatePayload,
  StockDetail,
  StockEntryPayload,
  StockMovementPayload,
} from "../types/stock";

export async function fetchImplementStock(implementId: number): Promise<StockDetail> {
  const response = await apiClient.get<StockDetail>(`/api/implements/${implementId}/stock`);
  return response.data;
}

export async function addStockEntry(
  implementId: number,
  payload: StockEntryPayload,
): Promise<StockDetail> {
  const response = await apiClient.post<StockDetail>(`/api/implements/${implementId}/stock/entries`, payload);
  return response.data;
}

export async function applyStockMovement(
  implementId: number,
  payload: StockMovementPayload,
): Promise<StockDetail> {
  const response = await apiClient.post<StockDetail>(`/api/implements/${implementId}/stock/movements`, payload);
  return response.data;
}

export async function updateIndividualState(
  implementId: number,
  individualId: number,
  payload: IndividualUpdatePayload,
): Promise<StockDetail> {
  const response = await apiClient.put<StockDetail>(
    `/api/implements/${implementId}/stock/individuals/${individualId}`,
    payload,
  );
  return response.data;
}

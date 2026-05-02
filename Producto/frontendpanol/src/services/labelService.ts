import { apiClient } from "./apiClient";

export type LabelScope = "GENERAL" | "INDIVIDUAL";

export async function fetchLabelsPdfBlob(
  implementId: number,
  scope: LabelScope,
  quantity = 1,
  individualId?: number,
): Promise<Blob> {
  const response = await apiClient.get(`/api/implements/${implementId}/labels/pdf`, {
    params: {
      quantity,
      scope,
      ...(individualId != null ? { individual_id: individualId } : {}),
    },
    responseType: "blob",
  });
  return response.data as Blob;
}

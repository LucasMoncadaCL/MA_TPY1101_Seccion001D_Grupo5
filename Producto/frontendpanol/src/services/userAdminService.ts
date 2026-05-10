import { apiClient } from "./apiClient";

export type AdminRole = "DIRECTOR" | "COORDINADOR" | "DOCENTE";

export interface UserAdminSummary {
  id: number;
  uuid: string | null;
  name: string;
  rut: string;
  email: string | null;
  role: AdminRole;
  active: boolean;
  createdAt: string;
}

export interface CreateUserPayload {
  name: string;
  rut: string;
  email?: string | null;
  role: "COORDINADOR" | "DOCENTE";
  password: string;
}

export interface UpdateUserPayload {
  name: string;
  rut: string;
  email?: string | null;
}

export async function listUsers(): Promise<UserAdminSummary[]> {
  const { data } = await apiClient.get<UserAdminSummary[]>("/api/v1/users");
  return data;
}

export async function createUser(payload: CreateUserPayload): Promise<void> {
  await apiClient.post("/api/v1/users", payload);
}

export async function changeUserRole(userRef: string, role: AdminRole): Promise<void> {
  await apiClient.put(`/api/v1/users/${userRef}/role`, { role });
}

export async function setUserActive(userRef: string, active: boolean): Promise<void> {
  await apiClient.patch(`/api/v1/users/${userRef}/active`, null, { params: { active } });
}

export async function updateUser(userRef: string, payload: UpdateUserPayload): Promise<void> {
  await apiClient.put(`/api/v1/users/${userRef}`, payload);
}

export async function deleteUser(userRef: string): Promise<void> {
  await apiClient.delete(`/api/v1/users/${userRef}`);
}

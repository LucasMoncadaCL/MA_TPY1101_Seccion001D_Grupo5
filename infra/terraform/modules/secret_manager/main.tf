resource "google_secret_manager_secret" "this" {
  for_each = toset(var.secrets)

  project   = var.project_id
  secret_id = each.value

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "this" {
  for_each = {
    for secret_id, secret_value in var.secret_values :
    secret_id => secret_value
    if contains(var.secrets, secret_id) && trimspace(secret_value) != ""
  }

  secret      = google_secret_manager_secret.this[each.key].id
  secret_data = each.value
}

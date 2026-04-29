variable "gcp_project_id" {
  type = string
}

variable "region" {
  type = string
}

variable "environment" {
  type = string
}

variable "artifact_registry_location" {
  type = string
}

variable "backend_image" {
  type = string
}

variable "frontend_image" {
  type = string
}

variable "supabase_db_host" {
  type = string
}

variable "supabase_db_port" {
  type = number
}

variable "supabase_db_name" {
  type = string
}

variable "supabase_db_user" {
  type = string
}

variable "supabase_db_ssl_mode" {
  type    = string
  default = "require"
}

variable "frontend_domain" {
  type = string
}

variable "backend_domain" {
  type = string
}

variable "backend_min_instances" {
  type    = number
  default = 0
}

variable "backend_max_instances" {
  type    = number
  default = 3
}

variable "backend_timeout_seconds" {
  type    = number
  default = 300
}

variable "backend_concurrency" {
  type    = number
  default = 80
}

variable "frontend_min_instances" {
  type    = number
  default = 0
}

variable "frontend_max_instances" {
  type    = number
  default = 3
}

variable "frontend_timeout_seconds" {
  type    = number
  default = 120
}

variable "frontend_concurrency" {
  type    = number
  default = 80
}

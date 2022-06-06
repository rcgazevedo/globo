resource "google_sql_database_instance" "main" {
  name             = "bbb-01"
  database_version = "POSTGRES_14"
  region           = "us-east1"
  project          = "globo-351616"
  root_password    = "bbb2022renato"

  settings {
    # Second-generation instance tiers are based on the machine
    # type. See argument reference below.
    tier = "db-f1-micro"
  }
}


data "google_client_config" "default" {}

provider "kubernetes" {
  host                   = "https://${module.gke.endpoint}"
  token                  = data.google_client_config.default.access_token
  cluster_ca_certificate = base64decode(module.gke.ca_certificate)
}
module "vpc" {
    source  = "terraform-google-modules/network/google"
    version = "~> 4.0"

    project_id   = "globo-351616"
    network_name = "vpc-01"
    routing_mode = "GLOBAL"

    subnets = [
        {
            subnet_name           = "us-east1-01"
            subnet_ip             = "10.0.0.0/23"
            subnet_region         = "us-east1"
        }


    ]
    secondary_ranges = {
        us-east1-01 = [
            {
                range_name    = "us-east1-01-gke-01-pods"
                ip_cidr_range = "10.1.0.0/19"
            },
            {
                range_name    = "us-east1-01-gke-01-services"
                ip_cidr_range = "172.16.0.0/22"
            },
        ]
    }
}

module "gke" {
  source                     = "terraform-google-modules/kubernetes-engine/google"
  project_id                 = "globo-351616"
  name                       = "globo-devops-bbb-1"
  region                     = "us-east1"
  zones                      = ["us-east1-d", "us-east1-b", "us-east1-c"]
  network                    = "vpc-01"
  subnetwork                 = "us-east1-01"
  ip_range_pods              = "us-east1-01-gke-01-pods"
  ip_range_services          = "us-east1-01-gke-01-services"
  http_load_balancing        = true
  network_policy             = false
  horizontal_pod_autoscaling = true
  filestore_csi_driver       = false
  default_max_pods_per_node  = 30

  node_pools = [
    {
      name                      = "default-node-pool"
      machine_type              = "e2-standard-16"
      node_locations            = "us-east1-d,us-east1-b,us-east1-c"
      min_count                 = 1
      max_count                 = 20
      local_ssd_count           = 0
      disk_size_gb              = 20
      disk_type                 = "pd-standard"
      image_type                = "COS_CONTAINERD"
      enable_gcfs               = false
      auto_repair               = true
      auto_upgrade              = true
      service_account           = "razevedo@globo-351616.iam.gserviceaccount.com"
      preemptible               = true
      initial_node_count        = 2
    },
  ]

  node_pools_oauth_scopes = {
    all = []

    default-node-pool = [
      "https://www.googleapis.com/auth/cloud-platform",
    ]
  }

  node_pools_labels = {
    all = {}

    default-node-pool = {
      default-node-pool = true
    }
  }

  node_pools_metadata = {
    all = {}

    default-node-pool = {
      node-pool-metadata-custom-value = "my-node-pool"
    }
  }

  node_pools_taints = {
    all = []

    default-node-pool = [
      {
        key    = "default-node-pool"
        value  = true
        effect = "PREFER_NO_SCHEDULE"
      },
    ]
  }

  node_pools_tags = {
    all = []

    default-node-pool = [
      "default-node-pool",
    ]
  }
}
##### These are critical, long-lived resources managed separately from ephemeral, environment-specific resources #####
##### DO NOT run `terraform destroy` once setup for an environment. There is no need to run this again for the same environment #####
provider "aws" {
  profile = "spm-orangle"
  region = var.aws_region
}

module "cognito" {
  source = "./cognito"
  environment = var.environment
  callback_urls = var.callback_urls
  user_pool_name = var.user_pool_name
  logout_urls = var.logout_urls
  project_name = var.project_name
  root_admin_email = var.root_admin_email
  root_admin_password = var.root_admin_password
  root_admin_phone_number = var.root_admin_phone_number
}

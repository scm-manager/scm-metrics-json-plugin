---
title: Documentation
subtitle: Json Metrics Plugin Documentation
---
This documentation describes the usage of Json Metrics Plugin. It is available in different languages and versions, which can be selected in the menu on the right.

In SCM Manager, many components collect metrics that provide useful information and can give insight into the usage of the system. This plugin provides an endpoint that provides the collected metrics in JSON format.

# Usage

The URL to this endpoint can be obtained from the index (`/api/v2`) of the respective SCM Manager instance. The link is located at `_links.metrics` with the name `json`.

To query the endpoint a valid authentication is required. If the endpoint is to be integrated into an external system, it is recommended to create a technical account in SCM Manager for authentication.

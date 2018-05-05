# Bote

Bote is a simple HTTP backend which can be used to manage newsletter subscriptions. It supports the following operations:

- Add a new email address ("subscription")
- Confirm a subscription
- Remove an email address

Unconfirmed subscriptions will be deleted after 24 hours.

After each of the above actions, a HTTP redirect is issued. The target URLs for these redirects can be configured individually.

Bote is licensed under Apache2 license and is provided by plapadoo (https://plapadoo.com)

## Build

use `mvn clean package` to build the jar file

## Configuration

All configuration options can be found in application.properties.

## Start

`java -jar target/bote-*-jar-with-dependencies.jar application.properties`

# route-service-sample


### Create and Bind Route Service to an app

```
cf create-user-provided-service route-service -r https://route-service.cfapps.io
cf bind-route-service <APPLICATION-DOMAIN> route-service --hostname <APPLICATION-HOST>
```

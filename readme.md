<p align="center">
<a href="https://ibb.co/bsNd2dW"><img src="https://i.ibb.co/zXPf7fn/Duke-Java-mascot-waving-svg.png" alt="Duke-Java-mascot-waving-svg" border="0" width="100"></a>
  <img src="https://seeklogo.com/images/S/spring-logo-9A2BC78AAF-seeklogo.com.png" width="140">
</p>

# SpringCloud Bank Microservices

Java Spring Cloud application for making transfers, deposits and payments as Bank System

### Tags
- Spring 
- Spring Boot
- Spring Cloud
- Microservices
- PostgreSQL
- RabbitMq
- REST
- AMQP

## Architecture
 <p>
  <a href="https://ibb.co/7n81mXs"><img src="https://i.ibb.co/N9QyBsc/architecture.png" alt="architecture" border="0" /></a>
</p>

*Description:`Configuration service` (Spring Cloud Config release) contains configuration information about all the rest microservices.
`Discovery service` (Eureka from Netflix) performs the functions of service discovery, registration of service addresses and their instances.
REST request from Client to `Gateway API` (Zuul from Netflix) (port 8989) is automatically redirected to necessary microservice.
Spring Cloud libraries contain Ribbon `Load Balancer`.
Feign `HTTP Client` from Spring Cloud used for http requests between database microcervices. For sending email message 
with information about succeed transactional is used RabbitMQ `message broker` with the help of Notification service.*

### Requests table

| METHOD | PATH | OPTION | NECESSARY FIELDS |
| ------:| :----- | :------: | :-----------:|
| `account-service (port 8081)`|
| **POST** | accounts/| create account | *name*, *email*, *phone*, *bills* (list of bills)
| **GET** | accounts/{accountId}| get account information by id | 
| **PUT** | accounts/{accountId}| update account |  *name*, *email*, *phone*, *bills* (list of bills)
| **PATCH** | accounts/{accountId}| add new bills to account |  *bills* (additional list of bills)
| **DELETE** | accounts/{accountId}| delete account | 
| `bill-service (port 8082)`|
| **POST** | bills/| create bill | *account_id*, *bill_id*, *amount*, *is_default*, *is_overdraft_enabled*
| **GET** | bills/{ billId}| get bill information by id | 
| **GET** | bills/account{accountId}| get bills information by account id | 
| **PUT** | bills/{ billId}| update account | *account_id*, *bill_id*(value doesn't affect), *amount*, *is_default*, *is_overdraft_enabled*  
| **DELETE** | bills/{ billId}| delete account |
| `deposit-service (port 9090)`|
| **POST** | deposits/| make deposit | *account_id* OR *bill_id*, *amount*
| **GET** | deposits/{depositId}| get deposit information by id |
| **GET** | deposits/bill/{billId}| get deposits information by bill id |
| `payment-service (port 9876)`|
| **POST** | payments/| make payment | *account_id* OR *bill_id*, *amount*
| **GET** | payments/{paymentId}| get payment information by id |
| **GET** | payments/bill/{billId}| get payments information by bill id |
| `transfer-service (port 5431)`|
| **POST** | transfers/| make transfer | *sender_bill_id* OR *sender_account_id*, *recipient_bill_id* OR *recipient_account_id*, *amount*
| **GET** | transfers/{transferId}| get transfer information by id |
| **GET** | transfers/sender/{senderBillId}| get transfers information by sender bill id |
| **GET** | transfers/recipient/{recipientBillId}| get transfers information by recipient bill id |

### How to start
###### Launch Application.java in  the following sequence:
`ConfigApplication` -> `RegistryApplication` -> `GatewayApplication` ->
`AccountApplication` -> `BillApplication` -> `NotificationApplication` ->
`DepositApplication` -> `PaymentApplication` -> `TransferApplication`

*PostgreSQL database and RabbitMQ requires personal settings*

###### Also application contain Dockerfiles and docker-compose.yml (configuration files to quick start with only several commands).
*It is possible to tune application quick start with Docker container if user have enough RAM*

### Necessary steps with JSON examples:
######1. Create account :


    {
        "name" : "Pavel",
        "email" : "example@mail.do",
        "phone" : "+123456789",
        "bills" : [ 1,3,5 ]
    }
    It will return account id: -> 1
    
    
######2. Create a bill : 
*Additional information: you can create the bill only with id that contain account bills list.*
*If you create first bill for account - this bill will be default anyway. If you create default bill but account has already existed default bill, existing default bill will change to not default.*  *


    {
        "account_id" :1,
        "bill_id": 3,
        "amount" : 10000,
        "is_default": false, 
        "is_overdraft_enabled" : false 
    }

######After first and second steps you can make deposits, payments or transfers(if you have at least two bills):

    Deposit or payment:
    {
        "account_id" : 1,
        "bill_id" : 3,
        "amount" : 5000
    }
    
    Transfer:
    {
        "sender_account_id" : 1,
        "recipient_bill_id" : 10,
        "amount" : 1500.50
    }




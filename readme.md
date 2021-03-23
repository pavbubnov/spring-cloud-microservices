<p align="center">
<a href="https://ibb.co/bsNd2dW"><img src="https://i.ibb.co/zXPf7fn/Duke-Java-mascot-waving-svg.png" alt="Duke-Java-mascot-waving-svg" border="0" width="100"></a>
  <img src="https://seeklogo.com/images/S/spring-logo-9A2BC78AAF-seeklogo.com.png" width="140">
</p>

# SpringCloud Bank Microservices

Java Spring Cloud application for making transfers, deposits and payments as Bank System

### Main capabilities: 
* Create person account
* Create a bill owned by account 
* Make deposits and payments to/from bill (with email notification)
* Transfer funds from one bill to another bill (with email notification to both accounts)

### Tags
- Spring 
- Spring Boot
- Spring Cloud
- Microservices
- PostgreSQL
- RabbitMq
- REST
- AMQP
- Eureka
- Zuul
- Feign
- Ribbon
- Docker

## Architecture
 <p>
  <a href="https://ibb.co/7n81mXs"><img src="https://i.ibb.co/N9QyBsc/architecture.png" alt="architecture" border="0" /></a>
</p>

*Description:`Configuration service` (Spring Cloud Config release) contains configuration information about all the rest microservices.
`Discovery service` (Eureka from Netflix) performs the functions of service discovery, registration of service addresses and their instances.
REST request from Client to `Gateway API` (Zuul from Netflix) (port 8989) is automatically redirected to necessary microservice.
Spring Cloud libraries contain Ribbon `Load Balancer`.
Feign `HTTP Client` from Spring Cloud used for http requests between microservices. For sending email message 
with information about succeed transactional is used RabbitMQ `message broker` with the help of Notification service.*

### Requests table

| METHOD | PATH | OPTION | NECESSARY FIELDS |
| ------:| :----- | :------: | :-----------:|
| `account-service (port 8081)`|
| **POST** | accounts/| create account | *name*, *email*, *phone*, *bills* (list of bills)
| **GET** | accounts/{accountId}| get account information by id | 
| **PUT** | accounts/{accountId}| update account |  *name*, *email*, *phone*, *bills* (list of bills)
| **PATCH** | accounts/{account
Id}| add new bills to account |  *bills* (additional list of bills)
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

You can quickly start application with docker: Run Clean and build tasks with Gradle tool ->  Run "docker-compose build" -> Run "docker-compose up"

*It is highly recommended to read full instruction below.*
###### 1. Actions before start :

a) Add email property file "mail-probs.properties" to "notification-service\src\main\resources" directory. 
This file contains information about no-reply email from which notifications  are sent.
Structure of this file:


    mail.username=example@gmail.com 
    mail.password=secretPassword
    mail.transport.protocol=smtp
    mail.smtp.auth=true
    mail.smtp.starttls.enable=true
    mail.debug=true`

*Don't forget to add this file to ".gitignore" for your git repository.*

*Additional security settings may be required in the mail profile.*

b) PostgreSQL properties:
*It is required to install PostqreSQL.*

b)1)Find and open "postgresql.conf" file at your computer - windows location approximately
 
C:\Program Files\PostgreSQL\8.4\data\postgresql.conf, where 8.4 - your PostgreSQL version 

b)2)Find line "listen_addresses" at "postgresql.conf" (pproximately line 59 ) and change to 

"listen_addresses = '*'		# what IP address(es) to listen on;"

b)3)Find and open "pg_hba.conf" at the same location

b)4) Add line "host    all             all              ::/0                   trust" at the end of "pg_hba.conf" file 

b)5) At config-service\src\main\resources\services directory are located configuration .yml files.
You can tune PostgreSQL access setting:

-Your password and username (default: postgres/admin).

-Name of pre-created databases. You can create database with the same name -> at this case you don't need to change 
database name (default name: account_service_database; bill_service_database, deposit_service_database,
payment_service_database, transfer_service_database).

-Port of your database (default: 5432).

###### 2.  It is allowed to start application from docker (if there is no need to start application from docker go to step 3) :
*It is required to install Docker.*

a) This application contains Dockerfile at each microservice package. One line in this files at account, bill, deposit,
payment and transfer services depends on the name of database and wireless network adapter ip. This is DATASOURCE_URL
variable. Default ip is 192.168.1.55, database names according b)5).
You can find your wireless network adapter ip by command line:
C:\Users>ipconfig -> database and wireless network adapter ip -> IPv4-adress

b) Run Clean and build tasks with Gradle tool (e.g. with Intellij IDEA)

c) Start Docker application.

c) Run "docker-compose build" command at spring-cloud-microservices folder. Wait for successful build.

d) Run "docker-compose up" command at spring-cloud-microservices folder. Wait for successful start.

*It is required to set more then 6GB memory in docker properties*

###### 3.  Sequential launch of application :
*It is required to install Docker.*

a) Start Docker application.

b) Run command "docker run -p 15672:15672 -p 5672:5672 rabbitmq:3-management"

c) Launch Application.java in  the following sequence:
`ConfigApplication` -> `RegistryApplication` -> `GatewayApplication` ->
`AccountApplication` -> `BillApplication` -> `NotificationApplication` ->
`DepositApplication` -> `PaymentApplication` -> `TransferApplication`

### Necessary steps with JSON examples:
###### 1. Create account :


    {
        "name" : "Pavel",
        "email" : "example@mail.do",
        "phone" : "+123456789",
        "bills" : [ 1,3,5 ]
    }
    It will return account id: -> 1
    
    
###### 2. Create a bill : 
*Additional information: you can create the bill only with id that contain account bills list.*
*If you create first bill for account - this bill will be default anyway. If you create default bill but account has already existed default bill, existing default bill will change to not default.*  *


    {
        "account_id" :1,
        "bill_id": 3,
        "amount" : 10000,
        "is_default": false, 
        "is_overdraft_enabled" : false 
    }

###### After first and second steps you can make deposits, payments or transfers(if you have at least two bills):

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


*Other command according "Requests table"*

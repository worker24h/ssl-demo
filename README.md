生成java用的证书，可以使用keytool生成，如果是其他类型的证书需要转换

<b>
keytool生成的证书默认是自签名的，也就是说它们不受任何公共证书颁发机构（CA）的信任。
这种证书在开发和测试环境中非常有用，但在生产环境中，最好使用由受信任的CA颁发的证书，以确保通信的安全性和可靠性。
</b>

keytool -genkey 和 -genkeypair 区别是什么
keytool命令中的-genkey和-genkeypair选项都用于生成密钥对和证书，但是它们的实现方式略有不同。

-genkey选项用于生成密钥对和自签名证书。它将提示您提供与密钥对和证书相关的信息，例如密钥密码、证书主题名称等。
生成的证书将是自签名的，这意味着它未经过任何CA的签名。该选项通常用于在测试或开发环境中生成自签名证书。
例如，以下命令将使用RSA算法和2048位密钥长度生成一个名为mykey的密钥对和自签名证书：
```bash
keytool -genkey -alias mykey -keyalg RSA -keysize 2048 -keystore mykeystore.jks
```

-genkeypair选项也用于生成密钥对和证书，但是它不会自动为证书签名。相反，它会生成一个证书签名请求(CSR)，您需要将此CSR发送给受信任的CA以获取由CA签名的证书。
这样生成的证书通常被认为比自签名证书更安全，因为它们由受信任的第三方签名。
例如，以下命令将使用RSA算法和2048位密钥长度生成一个名为mykey的密钥对和证书签名请求：
```bash
keytool -genkeypair -alias mykey -keyalg RSA -keysize 2048 -keystore mykeystore.jks
```
需要注意的是，使用-genkeypair选项生成的证书请求(CSR)需要另外的CA工具进行签名，而这不是keytool所提供的功能。


# 一、双向认证 自签证书生成
Keystore和Truststore区别：
Keystore用于通信，进行加解密
Truststore用于认证，认证对方是否有效

要使用keytool命令生成自签名的Keystore和Truststore，使其满足双向认证，可以按照以下步骤进行：
## 1. 生成服务端Keystore，包含服务端私钥和自签名证书。
```bash
keytool -genkey -alias server -keyalg RSA -keysize 2048 -validity 365 -keystore server_keystore.jks
```
在运行此命令时，您需要设置服务端私钥的密码以及一些与证书相关的信息，例如组织名称、所在城市等。

## 2. 导出服务端证书，生成证书文件。
```bash
keytool -export -alias server -file server.cer -keystore server_keystore.jks
```
该命令将服务端证书导出为一个X.509证书文件server.cer。

至此，服务端证书创建完毕，以相同流程，创建客户端证书
## 3. 生成客户端Keystore，包含客户端私钥和自签名证书。
```bash
keytool -genkey -alias client -keyalg RSA -keysize 2048 -validity 365 -keystore client_keystore.jks
```
在运行此命令时，您需要设置客户端私钥的密码以及一些与证书相关的信息，例如组织名称、所在城市等。

## 4. 导出客户端证书，生成证书文件。
```bash
keytool -export -alias client -file client.cer -keystore client_keystore.jks
```
该命令将客户端证书导出为一个X.509证书文件client.cer。

<b>下面是重点内容!!!</b>

## 5. 生成服务端Truststore，并将客户端证书导入Truststore。
```bash
keytool -import -alias client -file client.cer -keystore server_truststore.jks
```
此命令将<b>客户端证书</b>生成<b>服务端使用的</b>truststore文件，此处名字为server_truststore.jks
实际上是将客户端证保存到truststore文件

## 6. 生成客户端Truststore，并将服务端证书导入Truststore。
```bash
keytool -import -alias server -file server.cer -keystore client_truststore.jks
```
此命令将服务端证书导入到一个名为client_truststore.jks的Truststore文件中。

## 7. 因为我们用java代码编写客户端和服务端，最终只需要使用这四个文件即可
- server_keystore.jks    ==> 包含服务端的证书、服务端公钥、服务端私钥
- server_truststore.jks  ==> 包含客户端的证书以及客户端公钥
- client_keystore.jks    ==> 包含客户端的证书、客户端公钥、客户端私钥
- client_truststore.jks  ==> 包含服务端的证书以及客户端公钥
具体使用可参考TwoWay代码

# 二、单向认证 自签证书生成
由于是单向认证，有两种方式
## 1. 只需要将服务端的证书发给客户端使用即可
这里继续使用keytool命令生成自签名的Keystore，可以按照以下步骤进行：
```bash
keytool -genkey -alias myapp -keyalg RSA -keysize 2048 -validity 365 -keystore mykeystore.jks
```
具体使用可参考OneWay代码

## 2. 客户端预埋CA证书即可，不需要使用服务端的mykeystore.jks
这样有一种好处，对于终端设备来说，可以内置一个CA证书进行验证，这个CA证书有效期可以长一点，比如说10年，20年
而服务端证书可以短一些，比如1年，3年。因为终端设备升级需要进行OTA，比较麻烦。
这里采用openssl方式进行说明，更容易理解，当然keytool也可以实现

创建一对证书，用于签名服务器，10年
```bash
openssl genrsa -out ca.key 2048
openssl req -new -x509 -days 3650 -key ca.key -out ca.crt
```

生成服务器私钥和 CSR
```bash
openssl genrsa -out server.key 2048
openssl req -new -sha256 -key server.key -out server.csr 
```

对使用CA证书和CA私钥 对 服务器CSR进行签名，时间为1天
```bash
openssl x509 -req -sha256 -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out server.crt -days 1
```


使用keytool工具将server.crt添加到Truststore中，并且给tls服务端使用
```bash
# 这条命令不对，因为server.crt只包含证书，而且server.jks这里面需要包含私钥
keytool -import -alias myserver -file server.crt -keystore server.jks
```

```bash
# 这条命令是对的，KeyStore中必须要包含私钥，TrustStore中不能包含，否则私钥就泄露
# p12也是证书的一种
openssl pkcs12 -export -in server.crt -inkey server.key -out server.p12
keytool -importkeystore -srckeystore server.p12 -srcstoretype pkcs12 -destkeystore server.jks -deststoretype jks
```

使用keytool工具将ca.crt添加到Truststore中，并且给tls客户端使用
```bash
# 这条命令是对的，因为是客户端使用，客户端验证服务端是否可信，只需要证书即可，不需要私钥
# 如果客户端不埋入CA证书，则会直接报错无法建立连接
keytool -import -alias myca -file ca.crt -keystore ca.jks
```

上面创建服务端证书，有效期是1天，1天后就会过期。过期后不在验证通过



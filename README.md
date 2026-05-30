# Digital5 Backend API

## API Endpoints Documentation

### Account Controller (`/account`)

#### 1. **POST /account/register**
Registriert einen neuen Benutzer im System.

**Method:** POST  
**Path:** `/account/register`

**Request Body:**
```json
{
  "username": "string",
  "identityKey": "string",
  "preKey": "string",
  "preKeySignature": "string",
  "kemKey": "string",
  "keyKemSignature": "string",
  "oneTimeKeyPairs": [
    {
      
    }
  ]
}
```

**Response:**
- **Status:** 200 OK
- **Body:** `"Account registered successfully"`

---

#### 2. **GET /account/status**
Zeigt den Genehmigungsstatus des Kontos an. Erfordert JWT-Authentifizierung.

**Method:** GET  
**Path:** `/account/status`

**Request Body:**
```json
{
  "jwt": "string"
}
```

**Response:**
- **Status:** 200 OK
- **Body:** 
```json
{
  "account_status": "string"
}
```

**Throws:** `DigitalException` - Falls der Benutzer nicht authentifiziert ist oder nicht gefunden wird

---

### Message Controller (`/messages`)

#### 3. **POST /messages/send**
Sendet eine neue verschlüsselte Nachricht an einen Empfänger.

**Method:** POST  
**Path:** `/messages/send`

**Request Body:**
```json
{
  "jwt": "string",
  "recipient": "string",
  "encryptedHeader": "string",
  "messageBody": "string"
}
```

**Response:**
- **Status:** 200 OK

---

#### 4. **POST /messages/get**
Ruft alle Nachrichten eines Benutzers ab. Erfordert JWT-Authentifizierung.

**Method:** POST  
**Path:** `/messages/get`

**Request Body:**
```json
{
  "jwt": "string"
}
```

**Response:**
- **Status:** 200 OK
- **Body:** Liste aller Nachrichten des Benutzers

---

## Notes

- Alle Endpoints, die mit JWT arbeiten, erfordern ein gültiges JWT-Token im `jwt` Feld
- Die Endpoints sind verschlüsselungsfreundlich gestaltet und verwenden Public Key Cryptography


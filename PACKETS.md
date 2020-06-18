# Packet designs
All packets are handled over TCP. Values between [] are variables.  
Data between {} is encrypted with the private key of the sender, and is prepended by `HASH::[hash]\n`.   
[hash] is the hash of the message, encrypted with the private key of the sender.

## Gebruiker <> Gebruiker : wisselt ID uit
**Send**
```SEND::[id]::[timestamp]```  
**Received**
```CONFIRM::[id]::[timestamp]```

## Gebruiker > GGD : bevestigd contact
**Send**
```
SEND::CONTACT_CONF
{
    ID=[id]
    CONTACT_ID=[foreign_id]
    TIMESTAMP_CONTACT_SENT=[timestamp] // Gebruiker verzend contact met ID
    TIMESTAMP_CONTACT_CONFIRMED=[timestamp] // Andere gebruiker bevestigd contact naar gebruiker
}
```
**Received**
```
CONFIRM::CONTACT_CONF
{
    ID=[id]
    CONTACT_ID=[foreign_id]
    TIMESTAMP_CONTACT_SENT=[timestamp] // Gebruiker verzend contact met ID
    TIMESTAMP_CONTACT_CONFIRMED=[timestamp] // Andere gebruiker bevestigd contact naar gebruiker
    TIMESTAMP_GGD_CONFIRMED=[timestamp] // GGD bevestigd dat beide gebruikers bevestiging verzonden hebben
}
```

## Besmette gebruiker > GGD : bevestigd corona
**Send**
```
SEND::INFECT_CONF
{
    ID=[id]
    TIMESTAMP_SENT=[timestamp]
}
```
**Received**
```
CONFIRM::INFECT_CONF
{
    ID=[id]
    TIMESTAMP_SENT=[timestamp] // Gebruiker verzend besmetting
    TIMESTAMP_CONFIRMED=[timestamp] // GGD bevestigd dat besmetting is ontvangen
}
```

## GGD > Gebruiker : vraagt gegevens op en gebruiker verzend gegevens
**Send**
```
REQUEST::USER_DATA
{
    ID=[id]
    TIMESTAMP=[timestamp]
}
```
**Received**
```
SEND::USER_DATA
{
    ID=[id]
    FIRSTNAME=[f_name]
    LASTNAME=[l_name]
    BSN=[bsn]
    BIRTHDATE=[b_date]
    TIMESTAMP_RECEIVED=[timestamp]
    TIMESTAMP_SENT=[timestamp]
}
```
**Confirmed**
```
CONFIRM::USER_DATA
{
    ID=[id]
    FIRSTNAME=[f_name]
    LASTNAME=[l_name]
    BSN=[bsn]
    BIRTHDATE=[b_date]
    TIMESTAMP_RECEIVED=[timestamp]
    TIMESTAMP_SENT=[timestamp]
    TIMESTAMP_CONFIRMED=[timestamp]
}
```


## GGD > Gebruiker : verstuurd melding
**Send**
```
SEND::ALERT
{
    ID=[id]
    TIMESTAMP=[timestamp]
    CONTACT_TIMESTAMP=[timestamp]
}
```
**Received**
```
CONFIRM::ALERT
{
    ID=[id]
    TIMESTAMP=[timestamp]
    CONTACT_TIMESTAMP=[timestamp]
}
```

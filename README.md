# Corona App
## Uitgevoerd door andere teams
- Bluetooth connecties van apparaten (in de buurt van)
- GPS locatiebepaling van apparaten (idbv)
- Applicatie wordt al ontwikkelt voor alle platformen (wij alleen backend)

## Stappen
1. Gebruiker komt bij andere gebruiker in de buurt
2. Contact wordt vastgesteld en opgeslagen
3. Als gebruiker besmet is met corona 
 - wordt er een melding gegeven
 - doet GGD onderzoeker verder onderzoek naar andere mogelijke besmettingen
4. Een lijst aan contacten wordt aangemaakt welke :
 - een waarschuwing moet krijgen
 - moet worden doorgegeven aan de GGD
5. Secundaire contacten moeten opgevraagd kunnen worden, voor bovenstaande stap 4


## Eisen 
1. Persoonsgegevens moeten nooit onderschept kunnen worden
2. GGD'er moet zich identificeren voordat gegevens worden verzonden
3. Alleen GGD'er kan identiteit van gebruiker achterhalen + contacten (anderen alleen melding dat ze zich moeten laten testen)
4. Gebruikers moeten gegevens van contacten niet aan kunnen passen


## Assets
### Gebruiker
- U1: Eigen gegevens
- U2: Private key zelf
- U3: Public key GGD

### GGD
- G1: ID gebruiker
- G2: public key gebruiker
- G3: private key GGD

## Communicatie :
- C1: Beide gebruikers bevestigen contact (encrypt dmv private key gebruiker)
- C2: Indien match, sla op
- C3: Indien Corona :
  - C3a: GGD vraagt persoonsgegevens op (auth dmv priv key GGD, verifieer dmv pub key gebruiker)
  - C3b: GGD heeft al ID's van potentiele contacten, ook die worden opgevraagd
  - C3c: Gebruikers krijgen alleen melding van besmetting, geen ID's van de bron


## Notities
- Bevestiging vanuit gebruiker dat gegevens mogen worden opgeslagen na verzending

## Beoordeling :
 - Welke assets zijn er?  
   - ID
   - Contacten
   - Persoonsgegevens
   - Besmettingen

 - Wie moet waarvoor geïdentificeerd worden?
   - Gebruikers voor gegevens (door GGD)

 - Wie moet waarvoor geauthentiseerd worden?
   - GGD bij opvragen gegevens (door gebruiker)
   - GGD bij opsturen melding (door gebruiker)

 - Wie moet waarvoor geautoriseerd worden?
   - GGD bij opvragen gegevens (door backend)

 - Wat is er confidentieel en wat niet?
   - Persoonsgegevens wel
   - Besmettingen wel
   - ID niet, wordt direct gestuurd om contact te bevestigen

 - Van welke data moet de integriteit gewaarborgd worden?
   - Contact (icm persoonsgegevens bevestigd dit besmetting)
   - Persoonsgegevens (icm contact bevestigd dit besmetting)


## Threats 
- interruption (TCP - priv/pub)
- interception (priv/pub)
- modification (C3a)
- fabrication (C1)

## Vulnerabilities
- SQL Injection
- Één gebruiker geeft aan contact te hebben gehad, terwijl deze mogelijk nooit in de buurt is geweest (C1)


## Communicatie
### Verzonden
- CV1: Gebruikers wisselen ID's uit
- CV2: Gebruikers bevestigen contact aan GGD
- CV3: Besmette gebruiker informeert GGD
- CV4: GGD vraagt persoonsgegevens op
- CV5: Gebruiker verzend persoonsgegevens
- CV6: GGD stuurt melding

## Ontvangen
- CO1: Gebruiker ontvangt ID
- CO2: GGD ontvangt contact
- CO3: GGD ontvangt besmette gebruiker
- CO4: gebruiker ontvangt aanvraag persoonsgegevens
- CO5: GGD ontvangt persoonsgegevens
- CO6: gebruiker ontvangt melding


## Packet design

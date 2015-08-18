# Predviđanje cene proizvoda na eBay-u

##Opis problema

[eBay] (https://ebay.com/) je online aukcijski sajt i jedan od najpopularnijih sajtova na Internetu. Ima više miliona ponuda i više miliona korisnika koji se nadmeću i postavljaju (određuju) cene proizvoda. eBay, takođe, ima i besplatan XML-zasnovan [API] (https://go.developer.ebay.com/what-ebay-api) koji je moguće koristiti kako bi se izvršile razne pretrage, kako bi se dobili detaljni podaci o nekom proizvodu ili kako bi se neki proizvod postavio na sajt.

Osnovna ideja ovog projekta je kreiranje aplikacije koja će na osnovu prikupljenih podataka o proizvodima iz kategorije "Apple Laptops" predviđati cenu proizvoda na osnovu podataka o proizvodu. Za trening aplikacije koriste se četiri klasifikatora, a to su: k-Nearest-Neighbours, REPTree, Support Vector Machine (SVM) i NaiveBayes. U sekciji Analiza date su performanse svih testiranih klasifikatora i zaključci o njihovim performansama.

##Rešenje

Tok izvršenja aplikacije se sastoji iz sledećih koraka:

1.	Prikupljanje podataka o proizvodima

2.	Procesiranje prikupljenih podataka i kreiranje skupa podataka za trening

3.	Testiranje performansi klasifikatora


###Prikupljanje podataka o proizvodima

Za prikupljanje podataka o proizvodima iz kategorije "Apple Laptops" korišćen je [eBay Finding API] (http://developer.ebay.com/devzone/finding/concepts/findingapiguide.html). Ovaj servis pruža, pre svega, ID-jeve proizvoda koji će se kasnije koristiti za dobijanje više detalja o samom proizvodu. Link koji se koristi za poziv API-ja je u programu predstavljen sledećim stringom:

```
public final static String EBAY_FINDING_SERVICE_URI = "http://svcs.ebay.com/services/search/FindingService/v1?OPERATION-NAME="
			+ "{operation}&SERVICE-VERSION={version}&SECURITY-APPNAME="
			+ "{applicationId}&GLOBAL-ID={globalId}&categoryId={categoryId}"
			+ "&paginationInput.entriesPerPage={maxresults}"
			+ "&paginationInput.pageNumber={pagenumber}";
```

 - OPERATION-NAME je *findItemsByCategory* s obzirom da se prikupljaju podaci o proizvodima iz određene kateogrije 
 - SERVICE-VERSION je postavljen je na *1.0.0* i predstavlja verziju API-ja koju ova aplikacija podržava
 - SECURITY-APPNAME se postavlja na određenu vrednost koju se dobija prilikom registracije aplikacije
 - GLOBAL-ID je parametar koji predstavlja jedinstveni identifikator za kombinaciju sajta, jezika i teritorije; u ovom slučaju EBAY-US podrazumeva da će se koristiti eBay US sajt
 - CategoryId je postavljen na *111422* što predstavlja broj kategorije koja sadrži Apple laptopove
 - paginationInput.entriesPerPage parametar predstavlja broj rezultata koji će biti vraćeni u jednom pozivu. Postoji ograničenje koje se odnosi na ovaj parametar i podrazumeva da je u jednom pozivu API-ja moguće dobiti podatke o najviše 100 proizvoda
 - paginationInput.pageNumber parametar predstavlja broj stranice na kojoj se nalaze rezultati pretrage.
 
Kako bi se uopšte mogao izvršiti upit putem eBay API-ja, neophodno je prethodno registrovati aplikaciju na eBay sajtu koji je namenjen develoeprima. Kao rezultat registrovanja aplikacije na eBay sajtu, dobijaju se parametri koji se koriste prilikom poziva API-ja.

Više o tome kako se dobijaju brojevi kategorije može se pogledati na [GetCategoryInfo] (http://developer.ebay.com/devzone/shopping/docs/callref/GetCategoryInfo.html).  

Podrazumevani format u kojem eBay API vraća podatke je XML, tako da nije potrebno posebno naglasiti prilikom slanja zahteva da je ovo format u kojem će podaci biti vraćeni. Kako se u navedenom slučaju dobijaju podaci o 500 zapisa (5 strana sa po 100 zapisa), neophodno je iterirati kroz XML stablo i prikupiti podatke o proizvodima koji su potrebni za dalju realizaciju aplikacije. To je isključivo parametar *ItemID* koji će kasnije poslužiti da se dobiju detaljniji podaci o proizvodu.

Sledi primer dela XML odgovora:

```
...
<item>
	<itemId>131324038642</itemId>
	<title>Apple MacBook Pro Core i5 2.5GHz 4GB 500GB 13.3" MD101LL/A </title>
	<globalId>EBAY-US</globalId>
	<subtitle>Apple Certified Macbook Pro - Warranty - Free Shipping</subtitle>
	<primaryCategory>
		<categoryId>111422</categoryId>
		<categoryName>Apple Laptops</categoryName>
	</primaryCategory>
	…
	<productId type="ReferenceID">115174685</productId>
	<paymentMethod>PayPal</paymentMethod>
	<autoPay>true</autoPay>
	<postalCode>29681</postalCode>
	<location>Simpsonville,SC,USA</location>
	<country>US</country>
	<shippingInfo>
		<shippingServiceCost currencyId="USD">0.0</shippingServiceCost>
		<shippingType>FlatDomesticCalculatedInternational</shippingType>
		<shipToLocations>US</shipToLocations>
		…
		<shipToLocations>FR</shipToLocations>
		<expeditedShipping>true</expeditedShipping>
		<oneDayShippingAvailable>true</oneDayShippingAvailable>
		<handlingTime>1</handlingTime>
	</shippingInfo>
	<sellingStatus>
		<currentPrice currencyId="USD">669.99</currentPrice>
		<convertedCurrentPrice currencyId="USD">669.99</convertedCurrentPrice>
		<sellingState>Active</sellingState>
		<timeLeft>P28DT7H47M9S</timeLeft>
	</sellingStatus>
	<listingInfo>
		<bestOfferEnabled>false</bestOfferEnabled>
		<buyItNowAvailable>false</buyItNowAvailable>
		<startTime>2014-10-17T00:59:11.000Z</startTime>
		<endTime>2015-09-12T01:04:11.000Z</endTime>
		<listingType>StoreInventory</listingType>
		<gift>false</gift>
	</listingInfo>
	<returnsAccepted>true</returnsAccepted>
	<condition>
		<conditionId>2000</conditionId>
		<conditionDisplayName>Manufacturer refurbished</conditionDisplayName>
		</condition>
	<isMultiVariationListing>false</isMultiVariationListing>
	<topRatedListing>false</topRatedListing>
</item>
...
```

U cilju dobijanja detaljnijih informacija o proizvodima, na osnovu kojih će se kreirati dataset, neophodno je izvršiti poziv ka [eBay Shopping API-ju] (http://developer.ebay.com/devzone/shopping/docs/CallRef/index.html). Ovde će se iskoristiti podaci o ItemID-jevima proizvoda koji su prikupljeni prilikom pet poziva eBay Finding API-ja. S obzirom da se u tim pozivima dobijaju podaci o 500 proizvoda, potrebno je izvršiti 500 poziva ka eBay Shopping API-ju. U ovom slučaju link kojim se vrši poziv eBay Shopping API-ja je u programu prikazan sledećim stringom:

```
public final static String EBAY_FINDING_SERVICE_URI = "http://open.api.ebay.com/Shopping?callname="
			+ "{callname}&appid={appid}"
			+ "&version={version}"
			+ "&siteid={siteid}"
			+ "&ItemID={itemid}"
			+ "&IncludeSelector=Description,ItemSpecifics";
```

 - callname, u ovom slučaju je *GetSingleItem*. Ovaj poziv omogućava dobijanje detaljnijih podataka o proizvodima
 - appid je isti kao i u prethodnom primeru
 - version predstavlja verziju API-ja koju aplikacija podržava
 - siteid je *0* što predstavlja identifikator US sajta
 - ItemID je vrednost koja će biti promenjena u svih 500 poziva API-ju kako bi se dobili podaci o 500 različitih proizvoda
 - IncludeSelector=Description, ItemSpecifics podrazumeva da je potebno u odgovoru dobiti najdetaljnije podatke o opisu proizvoda

Kao i u prethodnom slučaju, podrazumevani format u kojem API vraća podatke je XML. Neophodno je proći kroz XML stablo i izvući sve podatke koji su neophodni za kreiranje dataset-a.

Sledi primer dela XML odgovora:

```
…
<Item>
	<Description> … </Description>
	<ItemID>111742104303</ItemID>
	<EndTime>2015-09-12T15:00:03.000Z</EndTime>
	<ViewItemURLForNaturalSearch>…</ViewItemURLForNaturalSearch>
	<ListingType>FixedPriceItem</ListingType>
	<Location>Jamaica, New York</Location>
	<GalleryURL>…</GalleryURL>    <PictureURL>…</PictureURL>
	<PrimaryCategoryID>111422</PrimaryCategoryID>
	<PrimaryCategoryName>Computers/Tablets &amp; Networking:Laptops &amp; Netbooks:Apple Laptops</PrimaryCategoryName
	<BidCount>0</BidCount>
	<ConvertedCurrentPrice currencyID="USD">1099.99</ConvertedCurrentPrice>
	<ListingStatus>Active</ListingStatus>
	<TimeLeft>P28DT21H29M55S</TimeLeft>
	<Title>Apple MacBook Pro MF839LL/A 13.3-Inch Laptop with Retina Display (128 GB) Newest</Title>
	<ItemSpecifics>
		…
		<NameValueList
			<Name>Brand</Name>
			<Value>Apple</Value>
		</NameValueList>
		<NameValueList>
			<Name>Product Family</Name>
			<Value>MacBook Pro</Value>
		</NameValueList>
		<NameValueList>
			<Name>Screen Size</Name>
			<Value>13.3&quot;</Value>
		</NameValueList>
		<NameValueList>
			<Name>Processor Type</Name>
			<Value>Intel Core i5</Value>
		</NameValueList>
		<NameValueList>
			<Name>Processor Speed</Name>
			<Value>2.70GHz</Value>
		</NameValueList>
		<NameValueList>
			<Name>Memory</Name>
			<Value>8GB</Value>
		</NameValueList>
		<NameValueList>
			<Name>Hard Drive Capacity</Name>
			<Value>128 GB</Value>
		</NameValueList>  
		…   
	</ItemSpecifics>
	<Country>US</Country>
	<AutoPay>true</AutoPay>
	<QuantityAvailableHint>Limited</QuantityAvailableHint>
	<GlobalShipping>true</GlobalShipping>
</Item> 
…
```

####Procesiranje prikupljenih podataka i kreiranje skupa podataka za trening

Samo procesiranje prethodno prikupljenih podataka vrši se u momentu kada se prolazi kroz XML stablo odgovora eBay API-ja.

Prilikom poziva eBay Finding API-ju nije potrebno da se podatak o ItemID-ju menja, jer je u formatu koji je i potreban.

U slučaju poziva eBay Shopping API-ja, podaci koji se dobijaju nisu u formatu koji je pogodan za kreiranje dataset-a pa ih je neophodno prilagoditi. U slučaju nominalnih podataka koji se odnose na vrednosti parametara Product Family, Processor Type i Operating System potrebno je izbaciti prazna mesta što se vrši pozivom metode:

replace(" ", ""); 

nad pomenutim stringovima. Podatku *ScreenSize* je takođe nephodno izbaciti znak koji označava inče (") kako bi ovaj podatak mogao imati numeričko tumačenje i ovo se realizuje pozivom metode:

replace("\"", "");

Nominalnom parametru *Operating System* se vrši zamena svih znakova zapeta sa srednjom crtom pozivom metode:

replace(",", "-");

Kod numeričkih parametara *Memory*, *Hard Drive Capacity* i *Processor Speed* neophodno je sačuvati samo deo koji se odnosi na brojeve što se vrši pozivom sledeće metode koja kao parametar prima regularni izraz koji izbacuje sve što nije broj iz stringa:

replaceAll("\\D+", "");

Kako bi se kreirao skup podataka koji će se koristiti za trening, neophodno je da se pozove metoda koja će vratiti podatke o 500 proizvoda koji su prikupiljeni (opisano u prethodnom poglavlju). Odgovarajućom obradom ovih podataka kreira se jedan [arff fajl] (http://www.cs.waikato.ac.nz/ml/weka/arff.html). Ovaj arff fajl sastoji se od nominalnih podataka porodica proizvoda, operativni sistem i tip procesora, kao i numeričkih podataka o veličini ekrana, memoriji, kapacitetu hard diska, brzini procesora i ceni.

Primer arff fajla:

```
@relation applelaptops 

@attribute productFamily {MacBookPro,MacBook,MacBookAir,Macbook}
@attribute operatingSystem {MacOSX10.7-Lion,MacOSX10.6-SnowLeopard,MacOSX10,MacOSX,MacOSX10.5-Leopard,MacOSX10.10-Yosemite,AppleMacOSX10.7,AppleMacOSX10.5,OSX:10.10.5,AppleMacOSX10.6,MacOSX10.4-Tiger,MacOSXYosemite,MacOSX10.10-4Yosemite,MacOSX10.9-Mavericks,AppleMacOSX10.4,MacOSX10.10.3-Yosemite,YOSEMITE10.10,MacOSXYOSEMITE10.10.3JUNE2015iLifeiWork,MacOSX10.8-MountainLion,AppleMacOSX,MacOSX10.9.4-Mavericks,OSX10.9.5,MacOSX10.10,AppleMacOSXMavericks,MacOSX10.10-3Yosemite,MacOSX10.9.5,Yosemite,OSXYosemite,MacOSYosemite,MacOSXYosemite10.10.4(TheLatest2015,OSX10.5,MacOSX10.9orOSX10.8,10.10.5,Silver,MacOSX10.7.5,MacOSX10.10Yosemite,OSX10.10Yosemite,MacOSX10.9.1,MacOSX10.9,osx,NotIncluded,MacOSX10.10.4-Yosemite,MacOSX10.6.3,MACOSX10.10.4Yosemite,MACOSX10.10.3YOSEMITE,MacOSX10.10.4Yosemite,Leopard}
@attribute processorType {IntelCorei5,IntelCore2Duo,Intel,IntelCorei72ndGen.,IntelCorei75thGen.,IntelCorei71stGen.,IntelCorei7,IntelCorei54thGen.,IntelCorei55thGen.,INTELCOREI7,IntelDualCorei7,IntelCoreI-54260U,IntelCorei51stGen.,IntelQuadCorei7,IntelCorei74thGen.,IntelCoreDuo,IntelCorei73rdGen.,IntelCorei52ndGen.,Core2Duo,IntelCorei53rdGen.,IntelQuad-Corei7,IntelCorei33rdGen.,IntelCoreM,IntelDualCore,IntelCorei7-3615QM,Corei7,IntelCorei54thGen,INTELCOREI5,Inteli7,inteli5processorwithboostspeedTechnology,IntelDual-Corei7,I7,500GB,IntelCorei5.,QUADCORE,I5,Corei5,IntelDual-Corei5,corei7,INTELCORE5,i5}
@attribute screenSize NUMERIC
@attribute memory NUMERIC
@attribute hardDriveCapacity NUMERIC
@attribute processorSpeed NUMERIC
@attribute productPrice NUMERIC

@data

MacBookPro,MacOSX10.7-Lion,IntelCorei5,13.3,4,500,250,669.99

MacBook,MacOSX10.6-SnowLeopard,IntelCore2Duo,13.3,2,250,226,259.99

...
```

Ovaj arff fajl sačuvan je u folderu "data" u okviru samog projekta. Nakon kreiranja ovog fajla izvršena je podela na dva nova fajla, a to su training.arff, koji se sastoji od 400 instanci koje služe za trening klasifikora i test.arff koji se sastoji od 100 instanci koje se koriste za testiranje klasifikatora.


###Testiranje performansi klasifikatora


Korišćena su četiri klasifikatora i to **k-Nearest-Neighbours**, **SupportVectorMachines**, **REPTree** i **NaiveBayes**. Trening klasifikatora se vrši nad dataset-om kreiranim u prethodnom koraku. Nakon uspešno izvršenog treninga, klasifikator treba da bude u stanju da što približnije predvidi cenu proizvoda na osnovu unetog seta parametara.


##Tehnička realizacija

Kod je realizovan u programskom jeziku **Java**. Referencirane biblioteke koje su korišćene u ovom projektu su **Weka** i **LibSVM**. 


###Weka

Ova biblioteka je korišćena za primenu metoda mašinskog učenja nad datasetovima čiji je proces kreiranja ranije opisan. U pitanju je čitav niz algoritama mašinskog učenja koji se koriste za izvršavanje DataMining zadataka (procesiranje velike količine podataka sa ciljem dobijanja novih informacija). U pitanju je "open source" softver. Link ka sajtu biblioteke: [Weka] (http://www.cs.waikato.ac.nz/ml/weka)

Sam proces treniranja klasifikatora se sastoji od učitavanja kreiranog arff fajla, zatim postavljanja atributa klase koji je u navedenom slučaju cena proizvoda, a zatim sledi kreiranje klasifikatora i evaluacija koja prikazuje podatke o performansama samog klasifikatora na konkretnom datasetu.

Nakon izvršenog treninga moguće je pozvati klasifikator da predvidi cenu proizvoda na osnovu parametara koji su uneti.


###LibSVM


[LibSVM] (https://www.csie.ntu.edu.tw/~cjlin/libsvm/) je biblioteka koja se koristi kako bi se kreirao klasifikator Support Vector Machine. Takođe, omogućava primenu linearne regresije nad podacima koji klasifikator koristi.


##Analiza

Sledi tabela u kojoj su dati podaci o koeficijentu korelacije i srednjoj apsolutnoj greški za sva četiri klasifikatora.

|Klasifikator|Koeficijent korelacije|Srednja apsolutna greška|
|------------|----------------------|------------------------|
|kNearestNeighbours|0,8493|124,0697|
|Support Vector Machine|0,3793|341,2433|
|REPTree|0,8053|182,5728|
|NaiveBayes|-0,4931|744,3484|

U cilju ocenjivanja određenog klasifikatora i upoređivanja sa nekim drugim klasifikatorom potrebno je da apsolutna vrednost koeficijenta korelacije bude što veća, odnosno da srednja apsolutna greška bude što manja. Generalno, koeficijent korelacije ocenjuje "jačinu" statističke veze između dve ili više promenljivih. S druge strane, srednja apsolutna greška je vrednost koja se koristi kako bi se izmerilo koliko su predviđanja približna mogućim ishodima.

Primetno je da je vrednost koeficijenta korelacije dosta mala, odnosno da je vrednost srednje apsolutne greške dosta velika kada se koristi Support Vector Machine klasifikator. Što se tiče koeficijenta korelacije kod klasifikatora k-Nearest-Neighbours i REPTree - oni imaju približne vrednosti, dok je srednja apsolutna greška ipak značajno manja kod klasifikatora kNearestNeighbours. S obzirom da je srednja apsolutna greška kod NaiveBayes klasifikatora jako velika njega cemo odmah odbaciti.

U skladu sa dobijenim podacima zaključuje se da je najbolje koristiti klasifikator k-Nearest-Neighbours, uz napomenu da i REPTree klasifikator ima dosta dobre performanse.

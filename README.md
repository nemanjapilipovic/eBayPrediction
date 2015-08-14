# Predviđanje cene proizvoda na eBay-u

##Opis problema

[eBay] (https://ebay.com/) je online aukcijski sajt i jedan od najpopularnijih sajtova na Internetu. Ima više miliona ponuda i više miliona korisnika koji se nadmeću i postavljaju (određuju) cene proizvoda. eBay, takođe, ima i besplatan XML-zasnovan [API] (https://go.developer.ebay.com/what-ebay-api) koji je moguće koristiti kako bi se izvršile razne pretrage, kako bi se dobili detaljni podaci o nekom proizvodu ili kako bi se neki proizvod postavio na sajt.

Osnovna ideja ovog projekta je kreiranje aplikacija koja će na osnovu prikupljenih podataka o proizvodima iz kategorije "Apple Laptops", moći da predvidi cenu proizvoda na osnovu unete grupe podataka o proizvodu od strane korisnika.
Za trening aplikacije koriste se tri klasifikatora, a to su: k-Nearest-Neighbours, REPTree i Support Vector Machine (SVM).

##Rešenje

Tok izvršenja aplikacije se sastoji iz sledećih koraka:

1.	Prikupljanje podataka o proizvodima

2	Procesiranje prethodno prikupljenih podataka

3.	Kreiranje skupa podataka za trening

4.	Primena metoda mašinskog učenja


###Prikupljanje podataka o proizvodima


Za prikupljanje podataka o proizvodima iz kategorije "Apple Laptops" korišćen je [eBay Finding API] (http://developer.ebay.com/devzone/finding/concepts/findingapiguide.html). Ovde se, pre svega, misli na prikupljanje ID-ja proizvoda kako bismo kasnije mogli dobiti više detalja vezanih za sam proizvod. Link koji služi za poziv API-ja je u programu predstavljen sledećim stringom:

public final static String EBAY_FINDING_SERVICE_URI = "http://svcs.ebay.com/services/search/FindingService/v1?OPERATION-NAME="
			+ "{operation}&SERVICE-VERSION={version}&SECURITY-APPNAME="
			+ "{applicationId}&GLOBAL-ID={globalId}&categoryId={categoryId}"
			+ "&paginationInput.entriesPerPage={maxresults}";

S obzirom da je potrebno da prikupimo podatke o proizvodima iz određene kategorije, parametar OPERATION-NAME je findItemsByCategory. Parametar SERVICE-VERSION je postavljan na 1.0.0 i predstavlja verziju API-ja koju ova aplikacija podržava. Kako bismo uopšte mogli da izvršimo upit putem eBay API-ja neophodno je da prethodno kreiramo aplikaciju na eBay sajtu koji je namenjen razvoju aplikacija što podrazumeva da smo dobili određene parametre koji će jedinstveno identifikovati upravo naše pozive API-ja. U našem slučaju to je parametar SECURITY-APPNAME koji postavljamo na određenu vrednost koju smo dobili prilikom registracije naše aplikacije. GLOBAL-ID je parametar koji predstavlja jedinstveni identifikator za kombinaciju sajta, jezika i teritorije. U našem slučaju EBAY-US podrazumeva da ćemo koristi eBay US sajt. CategoryId je postavljen na 111422 što predstavlja broj kategorije koja sadrži Apple laptopove. Više o tome kako se dobijaju brojevi kategorije možete pogledati na [GetCategoryInfo] (http://developer.ebay.com/devzone/shopping/docs/callref/GetCategoryInfo.html). I na kraju, paginationInput.entriesPerPage parametar predstavlja broj zapisa koji će biti vraćeni u jednom pozivu. 

Postoji ograničenje koje se odnosi na prethodno pomenuti parametar i koje podrazumeva da je u jednom pozivu API-ja moguće dobiti podatke o najviše 100 proizvoda.

Podrazumevani format u kojem eBay API vraća podatke je XML, tako da nije potrebno posebno naglasiti prilikom slanja zahteva da je ovo format u kojem želimo da dobijemo podatke. Kako u našem slučaju dobijamo podatke o 100 zapisa neophodno je da iteriramo kroz XML stablo i da prikupimo podatke o proizvodima koji su nam potrebni za dalju realizaciju naše aplikacije. To je isključivo parametar ItemID koji će nam kasnije poslužiti da dobijemo detaljnije podatke o proizvodu. Ovi podaci se čuvaju u polju tipa String.

Kako bismo dobili detaljnije informacije, o proizvodima, na osnovu kojih ćemo kreirati naše dataset-ove neophodno je izvršiti poziv ka [eBay Shopping API-ju] (http://developer.ebay.com/devzone/shopping/docs/CallRef/index.html). Ovde ćemo iskoristiti podatke o ItemID-jevima proizvoda koje smo sačuvali u String-u kada smo vršili poziv eBay Finding API-ja. S obzirom da smo u tom pozivu dobili podatke o 100 proizvoda, sada ćemo izvršiti 100 poziva ka eBay Shopping API-ju. U ovom slučaju link kojim se vrši poziv eBay Shopping API-ja u našem programu je prikazan sledećim Stringom:

public final static String EBAY_FINDING_SERVICE_URI = "http://open.api.ebay.com/Shopping?callname="
			+ "{callname}&appid={appid}"
			+ "&version={version}"
			+ "&siteid={siteid}"
			+ "&ItemID={itemid}"
			+ "&IncludeSelector=Description,ItemSpecifics";

Vrednost paramatra callname u ovom slučaju je GetSingleItem. Ovaj poziv omogućiće nam da dobijemo detaljnije podatke o proizvodima. Parametar appid je isti kao i u prethodnom slučaju. Verzija API-ja koju naša aplikacija podržava opisana je parametrom version. Vrednost siteid-ja je 0 što predstavlja identifikator US sajta. ItemID je vrednost koja će biti promenjena u svih 100 poziva API-ju kako bi se dobili podaci o 100 različitih proizvoda. IncludeSelector=Description, ItemSpecifics podrazumeva da želimo da u odgovoru dobijemo najdetaljnije podatke o opisu proizvoda.

Kao i u prethodnom slučaju, podrazumevani format u kojem API vraća podatke je XML. Neophodno je proći kroz XML stablo i izvući sve podatke koji su neophodni za kreiranje dataset-a. Ovi podaci su sačuvani, takođe, u polju koje je tipa String.


####Procesiranje prethodno prikupljenih podataka

Samo procesiranje prethodno prikupljenih podataka vrši se u momentu kada se prolazi kroz XML stablo odgovora eBay API-ja.

Prilikom poziva eBay Finding API-ju nije potrebno da se podatak o ItemID-ju menja, već se samo smešta u odgovarajući String.

U slučaju poziva eBay Shopping API-ja, podaci koje dobijamo nisu u formatu koji nam je pogodan za kreiranje dataset-a pa ih je neophodno prilagoditi našim potrebama. U slučaju nominalnih podataka koji se odnose na vrednosti parametara Product Family, Processor Type i Operating System potrebno je izbaciti prazna mesta što se vrši pozivom metode:

replace(" ", ""); 

nad pomenutim stringovima. Podatku ScreenSize je takođe nephodno izbaciti znak koji označava inče (") kako bi ovaj podatak mogao imati numeričko tumačenje i ovo ćemo realizovati pozivom metode:

replace("\"", "");

Nominalnom parametru Operating System se vrši zamena svim znakova zapeta sa srednjom crtom pozivom metode:

replace(",", "-");

Kod numeričkih parametara Memory, Hard Drive Capacity i Processor Speed neophodno je da sačuvamo samo deo koji se odnosi na brojeve što vršimo pozivom sledeće metode koja kao parametar prima regularni izraz koji izbacuje sve što nije broj iz stringa:

replaceAll("\\D+", "");


###Kreiranje skupa podataka za trening


Kako bismo kreirali skup podataka koji ćemo koristiti za trening neophodno je da pozovemo metodu koja će nam vratiti string koji će sadržati podatke o 100 proizvoda koje smo prikupili (opisano u prethodnom poglavlju). Odgovarajućom obradom ovih podataka kreiraćemo dva [arff fajla] (http://www.cs.waikato.ac.nz/ml/weka/arff.html). Prvi arff fajl sastoji se od nominalnih podataka porodica proizvoda, veličina ekrana, operativni sistem i tip procesora, kao i cene koja je numerička, dok se drugi arff fajl sastoji od isključivo numeričkih podataka o veličini ekrana, memoriji, kapacitetu hard diska, brzini procesora i ceni.

Primer prvog fajla:

@relation applelaptops 

@attribute screenSize NUMERIC
@attribute memory NUMERIC
@attribute hardDriveCapacity NUMERIC
@attribute processorSpeed NUMERIC
@attribute price NUMERIC

@data

13.3,4,500,250,669.99

13.3,2,250,240,269.99

...

Primer drugog fajla:

@relation applelaptops 

@attribute productFamily {MacBookPro,MacBook,MacBookAir}
@attribute screenSize {13.3,11.6,15.4,13,17}
@attribute operatingSystem {MacOSX10.7-Lion,MacOSX10.6-SnowLeopard,MacOSX10,MacOSX,MacOSX10.10-Yosemite,YOSEMITE,MacOSX10.5-Leopard,AppleMacOS,MacOSXYosemite,MacOS,OSXMavericks,MacOSX10.9-Mavericks}
@attribute processorType {IntelCorei5,IntelCore2Duo,IntelCorei52ndGen.,IntelCorei75thGen.,Quad-coreIntelCorei7,IntelDualCore,IntelCorei53rdGen.,Intel,IntelCorei73rdGen.,IntelCorei74thGen.,IntelCorei7,IntelCorei55thGen.,IntelCorei72ndGen.,IntelQuad-Corei7,IntelCorei71stGen.}
@attribute productPrice NUMERIC

@data

MacBookPro,13.3,MacOSX10.7-Lion,IntelCorei5,669.99

MacBook,13.3,MacOSX10.6-SnowLeopard,IntelCore2Duo,269.99

...

I jedan i drugi fajl sačuvani su u folderu "data" u okviru samog projekta.


###Primena metoda mašinskog učenja


Korišćena su tri klasifikatora i to **k-Nearest-Neighbours**, **SupportVectorMachines** i **REPTree**. Trening klasifikatora se vrši nad oba dataset-a kreirana u prethodnom koraku i za sva tri klasifikatora. Nakon uspešno izvršenog treninga klasifikator treba da bude u stanju da što približnije predvidi cenu proizvoda na osnovu unetog seta parametara.


##Tehnička analiza

Sledi opis biblioteka koje su korišćene u realizaciji samog projekta. Kod je realizovan u programskom jeziku **Java** korišćenjem razvojnog okruženja **Eclipse**. Referencirane biblioteke koje su korišćene u ovom projektu su **Weka** i **LibSVM**. 


###Weka

Ova biblioteka je korišćena za primenu metoda mašinskog učenja nad datasetovima čiji je proces kreiranja ranije opisan. U pitanju je čitav niz algoritama mašinskog učenja koji se koriste za izvršavanje DataMining zadataka (procesiranje velike količine podataka sa ciljem dobijanja novih informacija). U pitanju je "open source" softver. Link ka sajtu biblioteke: [Weka] (http://www.cs.waikato.ac.nz/ml/weka)

Sam proces treniranja klasifikatora se sastoji od učitavanja kreiranog arff fajla, zatim postavljanja atributa klase koji je u našem slučaju cena proizvoda, a zatim sledi kreiranje klasifikatora i evaluacija koja nam prikazuje podatke o performansama samog klasifikatora na konkretnom datasetu.

Nakon izvršenog treninga moguće je pozvati klasifikator da predvidi cenu proizvoda na osnovu parametara koje smo uneli.


###LibSVM


[LibSVM] (https://www.csie.ntu.edu.tw/~cjlin/libsvm/) je biblioteka koja se koristi kako bismo mogli da kreiramo klasifikator Support Vector Machine. Takođe, omogućava primenu linearne regresije nad podacima koji klasifikator koristi.


##Analiza

Slede tabele u kojima su dati podaci o koeficijentu korelacije i srednjoj apsolutnoj greški za sva tri klasifikatora, kao i za oba dataseta.

Tabela za numerički dataset:

|Klasifikator|Koeficijent korelacije|Srednja apsolutna greška|
|------------|----------------------|------------------------|
|kNearestNeighbours|0,8773|163,2821|
|Support Vector Machine|0,0299|433,4532|
|REPTree|0,8447|209,858|

Tabela za nominalni dataset:

|Klasifikator|Koeficijent korelacije|Srednja apsolutna greška|
|------------|----------------------|------------------------|
|kNearestNeighbours|0,8998|105,0033|
|Support Vector Machine|0,367|407,198|
|REPTree|0,8675|278,4448|

Kada su u pitanju oba dataset kako bismo ocenili određeni klasifikator i uporedili ga sa nekim drugim klasifikatorom potrebno je da apsolutna vrednost koeficijenta korelacije bude što veća, odnosno da srednja apsolutna greška bude što manja. Generalno, koeficijent korelacije ocenjuje "jačinu" statističke veze između dve ili više promenljivih. S druge strane, srednja apsolutna greška je vrednost koja se koristi kako bi se izmerilo koliko su predviđanja približna mogućim ishodima.

Na oba dataset-a je primetno da su vrednosti koeficijenta korelacije dosta male, odnosno da su vrednosti srednje apsolutne greške dosta velike kada se koristi Support Vector Machine klasifikator. Što se tiče koeficijenta korelacije kod klasifikatora kNearestNeighbours i REPTree - oni imaju približne vrednosti, dok su srednje apsolutne greške ipak značajno manje kod klasifikatora kNearestNeighbours.

U skladu sa podacima koje smo dobili možemo da zaključimo da je najbolje koristiti klasifikator kNearestNeighbours, uz napomenu da i REPTree klasifikator ima dosta dobre performanse.
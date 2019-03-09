# NUTS GeoSPARQL

## Production of the RDF files of the NUTS with their contours as WKT

The program produces Turtle files containing the European NUTS with their contours.

### Obtaining the NUTS

The NUTS reference files can be downloaded from RAMON, the Eurostat metadata repository. From the [home page](https://ec.europa.eu/eurostat/ramon), select the desired version, for example [NUTS (Nomenclature of Territorial Units for Statistics), by country, version 2016](https://ec.europa.eu/eurostat/ramon/nomenclatures/index.cfm?TargetUrl=ACT_OTH_DFLT_LAYOUT&StrNom=NUTS_2016&StrLanguageCode=EN). Click then on "Further files and information" to get to the download page. Select CSV and leave the default comma delimiter.

Save the file as NUTS_{year}_{scale}.csv (for example NUTS_2016_01.csv).

### Obtaining the contours

The NUTS contours can be produced from the Shape files available on [GISCO](https://ec.europa.eu/eurostat/web/gisco), more precisely on the [NUTS page](https://ec.europa.eu/eurostat/web/gisco/geodata/reference-data/administrative-units-statistical-units/nuts). Select the SHP archive at the desired reference year and scale and donwload it in a work directory. In the archive, extract the zip file having a name starting with NUTS_RG_{scale}, where {scale} is the desired scale (01, 03, 10, 20 or 60), and ending with _{epsg}.shp.zip, where {epsg} is the code of the desired coordinate reference system (see [http://www.epsg.org/](http://www.epsg.org/)). For example, for the NUTS 2016 contours at scale 1:60 Million in WGS84 coordinates, the zip file is named NUTS_RG_60M_2016_4326.shp.zip.

[QGIS](https://qgis.org/) can be used in order to exploit the Shape file. Select "Layer" -> "Add Layer" -> "Add Vector Layer" (or Ctrl+Shift+V) and chose the previous zip file as source. (Encoding UTF8?). Click on "Add" and close the data source manager. In the "Layers" view (down left), right click on the layer and select "Export" -> "Save Features As...". Chose CSV as output format and change the default layer options as follow:

  * GEOMETRY: AS_WKT
  * SEPARATOR: TAB

Save the file as NUTS_{year}_{scale}_WKT.csv (for example NUTS_2016_01_WKT.csv).
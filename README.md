# Twistoast

## Présentation
Twistoast est une application Android de consultation des horaires des bus & trams Twisto.

L'application est conçue pour améliorer l'expérience quotidienne des utilisateurs du réseau de
transports en commun de Caen.
L'existence d'une application officielle bien en-dessous de mes standards d'acceptabilité m'a poussé
au développement d'une alternative répondant aux critères suivants :

- Native, pas une simple application web lente, peu intégrée au système et peu fiable
- Design agréable à l'oeil et à l'utilisation
- Support des fonctionnalités déjà existantes dans l'application officielle
- Amélioration du service existant, avec par exemple la possibilité de mettre plus d'arrêts en
favoris que la limite de 5 arrêts imposée par le site et l'application officiels
- Fonctionnalités supplémentaires, comme le [support des montres Pebble](https://github.com/outadoc/Twistoast-pebble)
ou la réception de notifications pour informer de l'arrivée des bus

Ces fonctionnalités sont aujourd'hui toutes implémentées, et l'application est mise à jour
régulièrement pour corriger les bugs ou améliorer l'existant en ajoutant de nouvelles fonctionnalités
et en exploitant de nouvelles API. Il n'est également pas exclu de réimplémenter la récupération des
données si une API alternative se trouve être plus complète, plus stable ou plus adaptée.

N'hésitez pas à contribuer et à soumettre des pull requests si l'envie ou le besoin vous en prend !

## Captures d'écran
<img src="/../develop/assets/screenshots/fr_FR/screen_main.png?raw=true" width="280">
<img src="/../develop/assets/screenshots/fr_FR/screen_drawer.png?raw=true" width="280">
<img src="/../develop/assets/screenshots/fr_FR/screen_map.png?raw=true" width="280">

## Obtenir Twistoast
Twistoast est disponible sur le [Google Play Store](https://play.google.com/store/apps/details?id=fr.outadev.twistoast).

Pour compiler à partir des sources, clonez simplement le projet, et importez-le dans Android Studio.
Vous devrez ajouter une ressource string admob_adunitid et admob_test_device, qui correspondent
respectivement à la clé privée AdMob et l'identifiant AdMob d'un appareil de test.

## Développement
Twistoast a été porté depuis Java vers Kotlin afin de profiter des nombreuses features de ce langage, et améliorer
la stabilité de l'application au passage.
Si vous ne connaissez pas encore Kotlin, je vous invite à vous familiariser un peu avec le langage
avant d'essayer de contribuer au projet. Il sera assez familier aux développeurs Java.

## Documentation sur les APIs utilisées
Twistoast utilise les mêmes APIs que les services officiels. Vous pouvez trouver plus de détails sur
[ce gist](https://gist.github.com/outadoc/40060db45c436977a912) et [ce post de blog](https://outadoc.fr/2014/11/keolis-open-data-api/).

## Licence
Twistoast is released under the GNU GPL v3 license. Feel free to fork it and build upon it, as long
as you follow the rules!

    Copyright (C) 2013-2016 Baptiste Candellier

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

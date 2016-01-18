# Twistoast

## Présentation

Twistoast est une application Android de consultation des horaires des bus & trams Twisto.

L'application est conçue pour améliorer l'expérience quotidienne des utilisateurs du réseau de transports en commun de Caen.
Malgré l'existence d'une application officielle, je ressentais le besoin de l'améliorer pour qu'elle puisse :

- Être native, et pas une simple application web lente en peu intégrée au système
- Être élégante et bien conçue : malgré que je ne sois pas designer, je pense pouvoir faire facilement mieux que
l'application existante
- Améliorer le service proposé, avec par exemple la possibilité de mettre plus d'arrêts en favoris que la limite
de 5 arrêts imposée par le site et l'application officiels
- Proposer des fonctionnalités supplémentaires, comme le [support des montres Pebble](https://github.com/outadoc/Twistoast-pebble)
ou la réception de notifications pour informer de l'arrivée des bus

Ces fonctionnalités sont aujourd'hui toutes implémentées, et l'application est surtout mise à jour pour corriger les bugs ou
améliorer l'existant. N'hésitez cependant pas à contribuer et soumettre des pull requests si l'envie ou le besoin vous en prend !

## Obtenir Twistoast

Twistoast est disponible sur le [Google Play Store](https://play.google.com/store/apps/details?id=fr.outadev.twistoast) et
le [Chrome Web Store](https://chrome.google.com/webstore/detail/twistoast/olecaebebjbkmcnmobbdhgeicjfhidll?hl=fr).

Pour compiler à partir des sources, clonez simplement le projet, et importez-le dans Android Studio. Vous devrez ajouter une ressource
string admob_adunitid et admob_test_device, qui correspondent respectivement à la clé privée AdMob et l'identifiant AdMob d'un appareil de test.
Vous devriez pouvoir les laisser vides.

## Documentation de l'API utilisée

Twistoast utilise vraisemblablement les mêmes APIs que les services officiels. Vous pouvez trouver plus de détails sur
[ce gist](https://gist.github.com/outadoc/40060db45c436977a912) et [ce post de blog](https://outadoc.fr/2014/11/keolis-open-data-api/).

## Licence

Twistoast is released under the GNU GPL v3 license. Feel free to fork it and stuff, as long as you follow the rules!

    Copyright (C) 2013-2015  Baptiste Candellier

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

# COVID-19 Prediction and Visualization Software

## Software plots public data of the Corona (Covid-19) virus.

### Plot examples:
#### Configs: with country filter!
| ![Tornadoes](images/c1.JPG) | ![Tornadoes](images/d1.JPG) | ![Tornadoes](images/r1.JPG) |
| ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- |

##### Configs: *without* country filter!
| ![Tornadoes](images/c2.JPG) | ![Tornadoes](images/d2.JPG) | ![Tornadoes](images/r2.JPG) | 
| ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- |

##### Hover:
![Tornadoes](images/cc1.png)

### Code examples:
[Confirmed-Rate] Predict 10 day statistic for country: Germany.
```java
TimeSeriesDataPacket packet = this.getPacket("Germany", confirmedPackets);
System.out.println(packet);
assert packet != null;
this.predict(this.extractData(packet));
```
[Death-Rate] Predict 10 day statistic for country: Germany.
```java
TimeSeriesDataPacket packet = this.getPacket("Germany", deathPackets);
System.out.println(packet);
assert packet != null;
this.predict(this.extractData(packet));
```
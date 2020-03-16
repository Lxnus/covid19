package de.linusschmidt.covid19;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import de.linusschmidt.covid19.data.TimeSeriesDataPacket;
import de.linusschmidt.covid19.utilities.CSVUtilities;
import de.linusschmidt.covid19.utilities.DateComparator;
import de.linusschmidt.covid19.utilities.FileUtil;
import de.linusschmidt.covid19.utilities.Utilities;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.TimeSeriesPlot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {

    private FileUtil fileUtil;

    public Main() {
        this.fileUtil = new FileUtil();
    }

    public void load() throws IOException {
        // Loading data from csv files.
        List<TimeSeriesDataPacket> confirmedPackets = this.sumToCountry(this.loadTimeSeriesPackets("time_series_covid_19_confirmed.csv"));;
        List<TimeSeriesDataPacket> deathPackets = this.sumToCountry(this.loadTimeSeriesPackets("time_series_covid_19_deaths.csv"));
        List<TimeSeriesDataPacket> recoveredPackets = this.sumToCountry(this.loadTimeSeriesPackets("time_series_covid_19_recovered.csv"));

        // Filter. Shows only that countries in plot.
        String[] filter = new String[] {
                "Italy",
                "Germany",
                "China",
                "US"
        };

        // Data for prediction
        // List<List<Double>> confirmedTrainingData = this.extractData(confirmedPackets);
        // List<List<Double>> deathTrainingData = this.extractData(deathPackets);
        // List<List<Double>> recoveredTrainingData = this.extractData(recoveredPackets);

        // Plotting data
        this.plot("Confirmed", "Date", "Confirmed", confirmedPackets, filter);
        this.plot("Deaths", "Date", "Deaths", deathPackets, filter);
        this.plot("Recovered", "Date", "Recovered", recoveredPackets, filter);

        // Predict rate for next 10 days.
        TimeSeriesDataPacket packet = this.getPacket("Germany", confirmedPackets);
        System.out.println(packet);
        assert packet != null;
        this.predict(this.extractData(packet));
        System.out.println();

        packet = this.getPacket("Italy", confirmedPackets);
        System.out.println(packet);
        this.predict(this.extractData(packet));
        System.out.println();

        packet = this.getPacket("US", confirmedPackets);
        System.out.println(packet);
        this.predict(this.extractData(packet));
    }

    public void predict(List<Double> list) {
        System.out.println(list);

        // Example subList for prediction: Useful to test prediction
        list = list.subList(0, list.size() / 2 + list.size() / 3);

        System.out.println(list);

        double[] array = new double[list.size()];
        for(int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }

        // Predict next 10 days
        ForecastResult forecastResult = Arima.forecast_arima(array, 10, new ArimaParams(1, 3, 1, 1, 1, 0, 0));
        Utilities.printVector(forecastResult.getForecast());
    }

    /**
     * Returns requested TimeSeriesDataPacket
     * @param countryName name of country
     * @param packets list of packets to search in.
     * @return tsdp
     */
    private TimeSeriesDataPacket getPacket(String countryName, List<TimeSeriesDataPacket> packets) {
        for(TimeSeriesDataPacket packet : packets) {
            if(packet.getCountry().equalsIgnoreCase(countryName)) {
                return packet;
            }
        }
        return null;
    }

    /**
     * Extract data from tsdp for prediction.
     * @param packet input tsdp for extracting data.
     * @return extracted data.
     */
    private List<Double> extractData(TimeSeriesDataPacket packet) {
        List<Double> data = new ArrayList<>();
        for(String date : packet.getSeries().keySet()) {
            data.add(packet.getSeries().get(date));
        }
        return data;
    }

    /**
     * Extract data from packets.
     * @param packets input packets for extraction.
     * @return return extracted data.
     */
    private List<List<Double>> extractData(List<TimeSeriesDataPacket> packets) {
        List<List<Double>> matrix = new ArrayList<>();
        for(TimeSeriesDataPacket packet : packets) {
            List<Double> data = new ArrayList<>();
            for(String date : packet.getSeries().keySet()) {
                data.add(packet.getSeries().get(date));
            }
            matrix.add(data);
        }
        return matrix;
    }

    /**
     * Load and create TimeSeriesDataPacket's (tsdp).
     * @param fileName input file (csv)
     * @return list of tsdp.
     * @throws IOException
     */
    private List<TimeSeriesDataPacket> loadTimeSeriesPackets(String fileName) throws IOException {
        File file = this.fileUtil.createFile(fileName);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        int idx = 0;
        String line = "";
        List<String> descriptions = new ArrayList<>();
        List<TimeSeriesDataPacket> packets = new ArrayList<>();
        while((line = bufferedReader.readLine()) != null) {
            if(idx++ == 0) {
                descriptions = CSVUtilities.parseLine(line);
            } else {
                List<String> tokens = CSVUtilities.parseLine(line);
                String state = tokens.get(0);
                String country = tokens.get(1);
                double lat = Double.parseDouble(tokens.get(2));
                double lon = Double.parseDouble(tokens.get(3));
                TreeMap<String, Double> series = new TreeMap<>(new DateComparator());
                for(int i = 4; i < tokens.size(); i++) {
                    series.put(descriptions.get(i), Double.parseDouble(tokens.get(i)));
                }
                TimeSeriesDataPacket timeSeriesDataPacket = new TimeSeriesDataPacket(state, country, lat, lon, series);
                packets.add(timeSeriesDataPacket);
            }
        }
        return packets;
    }

    /**
     * Sums data up from a country. As example sum all states from US up to one.
     * @param packets packets to sum up (clean up)
     * @return no duplicates in list (clear packets).
     */
    private List<TimeSeriesDataPacket> sumToCountry(List<TimeSeriesDataPacket> packets) {
        HashMap<String, List<TimeSeriesDataPacket>> map = new HashMap<>();
        for(TimeSeriesDataPacket packet : packets) {
            List<TimeSeriesDataPacket> mapPackets = map.get(packet.getCountry());
            if (mapPackets == null) {
                mapPackets = new ArrayList<>();
            }
            mapPackets.add(packet);
            map.put(packet.getCountry(), mapPackets);
        }
        List<TimeSeriesDataPacket> resultPackets = new ArrayList<>();
        for(String country : map.keySet()) {
            TreeMap<String, Double> series = new TreeMap<>(new DateComparator());
            for(TimeSeriesDataPacket packet : map.get(country)) {
                packet.getSeries().forEach((k, v) -> series.merge(k, v, Double::sum));
            }
            TimeSeriesDataPacket resultPacket = new TimeSeriesDataPacket();
            resultPacket.setCountry(country);
            resultPacket.setSeries(series);
            resultPackets.add(resultPacket);
        }
        return resultPackets;
    }

    /**
     * Plotting data.
     * @param title name of plot
     * @param x name of the xCol
     * @param y name of the yCol
     * @param packets that are represented in the plot
     * @param filterCountries show's only the countries from array. NULL to show all.
     */
    private void plot(String title, String x, String y, List<TimeSeriesDataPacket> packets, String[] filterCountries) {
        Table table = Table.create();
        table.addColumns(DateColumn.create(x));
        table.addColumns(DoubleColumn.create(y));
        table.addColumns(StringColumn.create("Country"));
        for(String date : packets.get(0).getSeries().keySet()) {
            for(TimeSeriesDataPacket packet : packets) {
                DateColumn strings = (DateColumn) table.column(0);
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yy");
                strings.append(LocalDate.parse(date, dateTimeFormatter));

                DoubleColumn doubles = (DoubleColumn) table.column(1);
                doubles.append(packet.getSeries().get(date));

                StringColumn country = (StringColumn) table.column(2);
                country.append(packet.getCountry());
            }
        }

        // Filter
        if(filterCountries != null) {
            Table filter = table.where(table.stringColumn("Country").isIn(filterCountries));
            Plot.show(TimeSeriesPlot.create(title, filter, x, y, "Country"));
        } else {
            Plot.show(TimeSeriesPlot.create(title, table, x, y, "Country"));
        }
    }

    public static String getFramework_Name() {
        return "COVID-19";
    }

    public static void main(String[] args) throws IOException {
        Main loader = new Main();
        loader.load();
    }
}

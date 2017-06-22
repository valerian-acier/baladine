package com.balady.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.balady.data.utils.TypeZone;
import com.balady.population.Consumer;
import com.balady.rest.ClientRest;
import com.balady.rest.receiptJson.DrinkInfo;
import com.balady.rest.receiptJson.Forecast;
import com.balady.rest.receiptJson.MapItem;
import com.balady.rest.receiptJson.PlayerInfo;
import com.balady.rest.receiptJson.ReceiptObject;
import com.balady.rest.receiptJson.TimeReceipt;

public class GameMap {

	private String meteo;
	private long hour;
	private List<Player> players;
	private Coordinates start;
	private Coordinates end;
	private ClientRest rest;
	private Map<String, Sale> sales;
	private List<Consumer> consumers;

	public GameMap(String url) {
		rest = new ClientRest(url);
		refreshMap();
		consumers = new ArrayList<>();
		addCustomers();
	}

	/**
	 * @return the sales
	 */
	public Map<String, Sale> getSales() {
		return sales;
	}

	/**
	 * @param sales
	 *            the sales to set
	 */
	public void setSales(Map<String, Sale> sales) {
		this.sales = sales;
	}

	/**
	 * @return the meteo
	 */
	public String getMeteo() {
		return meteo;
	}

	/**
	 * @param meteo
	 *            the meteo to set
	 */
	public void setMeteo(String meteo) {
		this.meteo = meteo;
	}

	/**
	 * @return the players
	 */
	public List<Player> getPlayers() {
		return players;
	}

	/**
	 * @param players
	 *            the players to set
	 */
	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	/**
	 * @return the start
	 */
	public Coordinates getStart() {
		return start;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(Coordinates start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public Coordinates getEnd() {
		return end;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public void setEnd(Coordinates end) {
		this.end = end;
	}

	/**
	 * @return the hour
	 */
	public long getHour() {
		return hour;
	}

	/**
	 * @param hour
	 *            the hour to set
	 */
	public void setHour(long hour) {
		this.hour = hour;
	}

	/**
	 * @return the rest
	 */
	public ClientRest getRest() {
		return rest;
	}

	/**
	 * @param rest
	 *            the rest to set
	 */
	public void setRest(ClientRest rest) {
		this.rest = rest;
	}

	public void refreshMap() {
		players = new ArrayList<>();

		// Fill Hour and Meteo
		TimeReceipt time = rest.getMeteorologyAndTime();
		for (Forecast f : time.getWeather()) {
			if (f.getDfn() == 0)
				meteo = f.getWeather();
		}
		hour = time.getTimestamp() / 24;
		ReceiptObject receipt = rest.getData();

		// Fill Position of Map
		start = new Coordinates(receipt.getRegion().getCenter().getLongitude(),
				receipt.getRegion().getCenter().getLatitude());
		end = new Coordinates(receipt.getRegion().getspan().getLongitudeSpan(),
				receipt.getRegion().getspan().getLatitudeSpan());

		// Fill PlayersInfo
		for (String name : receipt.getRanking()) {
			if (receipt.getPlayerInfo().containsKey(name)) {
				PlayerInfo player = receipt.getPlayerInfo().get(name);

				// Fill Zones info to Player
				List<Zone> zones = new ArrayList<>();
				if (receipt.getItemsByPlayer().containsKey(name)) {
					for (MapItem item : receipt.getItemsByPlayer().get(name)) {
						TypeZone typeZone;
						if ("ad".equals(item.getKind()))
							typeZone = TypeZone.PUB;
						else
							typeZone = TypeZone.STAND;
						zones.add(new Zone(item.getInfluencce(), typeZone,
								new Coordinates(item.getLocation().getLongitude(), item.getLocation().getLatitude())));
					}
				}

				// Fill Drinks info to Player
				List<Drink> drinks = new ArrayList<>();
				if (receipt.getDrinksByPlayer().containsKey(name)) {
					for (DrinkInfo drink : receipt.getDrinksByPlayer().get(name)) {
						drinks.add(new Drink(drink.getName(), drink.getPrice(), drink.HasAlcohol(), drink.isCold()));
					}
				}
				players.add(new Player(name, player.getCash(), player.getProfit(), player.getSales(), zones, drinks));
			}
		}
	}

	/**
	 * Make one turn (If the consumer drink and when the consumer move)
	 */
	public void play() {
		for (Consumer c : consumers) {
			for (Player p : players) {
				for (Zone z : p.getZones()) {
					if (z.isInInfluence(c) && !p.getDrinks().isEmpty()) {
						if (sales.containsKey(p.getName() + "/" + p.getDrinks().get(0))) {
							Sale sale = sales.get(p.getName() + "/" + p.getDrinks().get(0));
							sale.setNb(sale.getNb() + 1);
						} else
							sales.put(p.getName() + "/" + p.getDrinks().get(0),
									new Sale(p.getName(), p.getDrinks().get(0).getName(), 1));
					}
				}
			}
		}
	}

	/**
	 * Send sales to Server python
	 */
	public void sendSales() {
		List<Sale> listSales = new ArrayList<>();
		for (Entry<String, Sale> tmp : this.sales.entrySet()) {
			listSales.add(tmp.getValue());
		}
		rest.sendSales(listSales);
		sales.clear();
	}

	public void addCustomers() {
		
		int nbPop;
		
		switch (meteo) {
		case ("Soleil"):
			nbPop = 75;
			break;
		case ("Orage"):
			nbPop = 0;
			break;
		case ("Nuage"):
			nbPop = 30;
			break;
		case ("Canicule"):
			nbPop = 100;
			break;
		default:
			nbPop = 15;
			break;
		}
		for (int i = 0; i < nbPop; i++) {
			consumers.add(new Consumer(start.getX(), end.getX(), start.getY(), end.getY()));
		}
	}
}

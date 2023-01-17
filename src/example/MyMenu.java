package example;

import java.awt.Menu;
import java.util.ArrayList;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import mindustry.game.Team;
import mindustry.content.StatusEffects;
import mindustry.game.EventType.MenuOptionChooseEvent;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.net.Administration.Config;
import mindustry.type.StatusEffect;

import static mindustry.Vars.*;

public class MyMenu {
	
	enum MenuOption {
		OpenMapManager,
		AdminPlayerManager,
		AdminPlayerConfig,
		AdminPlayerConfigAction,
		Other
	}
	
	ArrayList<Menu> menus;

	public MyMenu() {
		menus = new ArrayList<>();
        Events.on(MenuOptionChooseEvent.class, e -> {
        	Log.info(e.menuId + ": " + e.option);
        	
        	Menu menu = null;
        	for (int i = 0; i < menus.size(); i++) {
				menu = menus.get(i);
				if(menu.id == e.menuId) {
					menus.remove(menu);
					break;
				}
			}
        	
        	if(e.option < 0) return;
        	
        	if(menu != null) {
        		if(e.option < menu.menuOptions.length) {
        			Log.info("Option: " + menu.menuOptions[e.option].name());
        			onButtonPressed(menu, menu.menuOptions[e.option].ordinal(), e.option);
        		}
        	}
        });
	}


	public void update() {
	}
	
	public void onButtonPressed(Menu menu, int optionID, int clickedId) {
		
		if(!menu.player.admin()) return;

		if(optionID == MenuOption.AdminPlayerManager.ordinal()) {
			String players[][] = new String[Groups.player.size()][];
			MenuOption[] menuOption = new MenuOption[Groups.player.size()];
			for (int i = 0; i < Groups.player.size(); i++) {
				menuOption[i] = MenuOption.AdminPlayerConfig;
				Player player = Groups.player.index(i);
				if(player == null) continue;
				if(i < players.length) {
					players[i] = new String[] {player.coloredName()};
				}
			}
			menu(menu.player, "Управление игроками", "Выберите игрока", players, menuOption);
		}
		
		if(optionID == MenuOption.AdminPlayerConfig.ordinal()) {
			String playerName = null;
			
			int id = 0;
			for (int i = 0; i < menu.options.length; i++) {
				for (int j = 0; j < menu.options[i].length; j++) {
					if(id == clickedId) {
						playerName = menu.options[i][j];
					}
					id++;
				}
			}
			
			String players[] = new String[Groups.player.size()];
			
			for (int i = 0; i < Groups.player.size(); i++) {
				Player player = Groups.player.index(i);
				if(player == null) continue;
				if(i < players.length) {
					players[i] = player.coloredName();
				}
			}
			if(playerName == null) return;
			
			String[] teams = new String[Team.baseTeams.length];
			MenuOption[] menuOptions = new MenuOption[Team.baseTeams.length+2];
			for (int i = 0; i < teams.length; i++) {
				
				char ch = ' ';

				if(Team.baseTeams[i] == Team.derelict) ch = '\uf77e';
				if(Team.baseTeams[i] == Team.sharded) ch = '\uf77c';
				if(Team.baseTeams[i] == Team.malis) ch = '\uf6a9';
				if(Team.baseTeams[i] == Team.crux) ch = '\uf7a9';
				
				if(ch == ' ') {
					teams[i] = "[#" + Team.baseTeams[i].color + "]\ue80e";
				} else {
					teams[i] = ch + "";
				}
			}
			
			for (int i = 0; i < menuOptions.length; i++) {
				menuOptions[i] = MenuOption.AdminPlayerConfigAction;
			}
			
			
			menu(menu.player, "Управление игроком", playerName, new String[][] {teams, {"[green]\ue80f Вылечить", "[royal]\ue86b Неуязвимость"}}, menuOptions).payload = playerName;
		}
		if(optionID == MenuOption.AdminPlayerConfigAction.ordinal()) {
			Log.info(menu.payload);
			if(menu.payload == null) return;
			Player targetPlayer = Groups.player.find(p -> Strings.stripColors(p.name()).equalsIgnoreCase(Strings.stripColors(menu.payload)));
			if(targetPlayer == null) return;
			
			
			int teamsCount = Team.baseTeams.length;
			if(clickedId < teamsCount) {
				Team team = Team.baseTeams[clickedId];
				targetPlayer.team(team);
			}
			if(clickedId == teamsCount) {
				targetPlayer.unit().heal();
			}
			if(clickedId == teamsCount+1) {
				targetPlayer.unit().apply(StatusEffects.invincible, Float.MAX_VALUE);
			}
		}
	}
	
	public void registerCommand(CommandHandler handler) {
    	handler.<Player>register("m", "", "Открыть меню", (args, player) -> {
    		if(player.admin()) {
        		menu(player, Config.serverName.get().toString(), "", new String[][] {{"Управление игроками"}}, new MenuOption[] {MenuOption.AdminPlayerManager});
    		} else {
    		}
    	});
	}
	
	int nextMenuId = 0;
	private Menu menu(Player player, String title, String text, String[][] options, MenuOption[] menuOptions) {
		Menu menu = new Menu(player, nextMenuId, options, menuOptions);
		menus.add(menu);
		Call.menu(player.con(), nextMenuId, title, text, options);
		nextMenuId++;
		return menu;
	}
	
	
	class Menu {

		public String payload;
		String[][] options;
		MenuOption[] menuOptions;
		Player player;
		int id;
		
		Menu(Player player, int id, String[][] options, MenuOption[] menuOptions) {
			this.menuOptions = menuOptions;
			this.options = options;
			this.player = player;
			this.id = id;
		}
		
	}

}

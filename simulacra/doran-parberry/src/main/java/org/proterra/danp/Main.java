package;

import bazaarbot.Economy;
import bazaarbot.Market;
import bazaarbot.utils.Quick;
import flash.Lib;
import flash.text.TextField;
import flash.text.TextFieldAutoSize;
import flash.text.TextFormat;
import flash.display.SimpleButton;
import flash.display.Sprite;
import openfl.Assets;
import flash.events.MouseEvent;
import flash.text.TextFormatAlign;


class Main extends Sprite
{
	private var economy:Economy;
	private var market:Market;
	private var display:MarketDisplay;
	private var txt_benchmark:TextField;
	
	public function new ()
	{
		super ();
		
		economy = new DoranAndParberryEconomy();
		
		market = economy.getMarket("default");
	
	}
	

	

	
}

package ddsl.dobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DButton extends DObject {
	
	
	public DButton(WebDriver driver, WebElement element) {
		super(driver, element);
	}
	
	@Override
	public String getText() {
		String spanText = element.findElement(By.cssSelector("span > span")).getText().trim();
		return spanText;
	}
	
}

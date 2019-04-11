package spring.mine.siteinformation.form;

import java.util.List;

import spring.mine.common.form.MenuForm;

public class ResultConfigurationMenuForm extends MenuForm {
	private List menuList;

	private String[] selectedIDs;

	private String siteInfoDomainName = "ResultConfiguration";

	public ResultConfigurationMenuForm() {
		setFormName("resultConfigurationMenuForm");
	}

	@Override
	public List getMenuList() {
		return menuList;
	}

	@Override
	public void setMenuList(List menuList) {
		this.menuList = menuList;
	}

	@Override
	public String[] getSelectedIDs() {
		return selectedIDs;
	}

	@Override
	public void setSelectedIDs(String[] selectedIDs) {
		this.selectedIDs = selectedIDs;
	}

	public String getSiteInfoDomainName() {
		return siteInfoDomainName;
	}

	public void setSiteInfoDomainName(String siteInfoDomainName) {
		this.siteInfoDomainName = siteInfoDomainName;
	}
}
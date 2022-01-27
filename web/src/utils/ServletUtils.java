package utils;

//import engine.chat.ChatManager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import managers.Manager;
import managers.UserManager;

import static utils.Constants.MANAGER_ATTRIBUTE_NAME;
import static utils.Constants.USER_MANAGER_ATTRIBUTE_NAME;

//import static chat.constants.Constants.INT_PARAMETER_ERROR;

public class ServletUtils {
	/*
	Note how the synchronization is done only on the question and\or creation of the relevant managers and once they exists -
	the actual fetch of them is remained un-synchronized for performance POV
	 */
	private static final Object userManagerLock = new Object();
	private static final Object managerLock = new Object();
	private static final Object chatManagerLock = new Object();

	public static UserManager getUserManager(ServletContext servletContext) {

		synchronized (userManagerLock) {
			if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
			}
		}
		return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
	}

	public static Manager getManager(ServletContext servletContext) {

		synchronized (managerLock) {
			if (servletContext.getAttribute(MANAGER_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(MANAGER_ATTRIBUTE_NAME, new Manager());
			}
		}
		return (Manager) servletContext.getAttribute(MANAGER_ATTRIBUTE_NAME);
	}

//	public static ChatManager getChatManager(ServletContext servletContext) {
//		synchronized (chatManagerLock) {
//			if (servletContext.getAttribute(CHAT_MANAGER_ATTRIBUTE_NAME) == null) {
//				servletContext.setAttribute(CHAT_MANAGER_ATTRIBUTE_NAME, new ChatManager());
//			}
//		}
//		return (ChatManager) servletContext.getAttribute(CHAT_MANAGER_ATTRIBUTE_NAME);
//	}

	public static int getIntParameter(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException numberFormatException) {
			}
		}
		return Integer.MIN_VALUE;
	}
}

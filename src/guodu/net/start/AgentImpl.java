package guodu.net.start;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import guodu.net.impl.AnalyseImpl;
import guodu.net.impl.AnalyselmplEx;
import guodu.net.util.Loger;

/**
 * 信息分析代理部分
 * 获取指定package包下所有class文件，执行其analyse方法
 * 本方法采用代理＋适配器模式，所以AnalyseImpl接口的各实
 * 现类中的analyse方法不是强制重写，但是开发是需要注意
 * analyse方法必须要重写才能正常运行。
 * */
public class AgentImpl extends AnalyselmplEx{

	private AnalyseImpl ai;
	private String packageName;
	
	public AgentImpl(AnalyseImpl ai , String packageName){
		this.ai = ai;
		this.packageName = packageName;
	}
	
	@Override
	public void analyse() {
		@SuppressWarnings("rawtypes")
		List<Class> listClass = null;
		try {
			listClass = getClassList();
		} catch (Exception e) {
			e.printStackTrace();
			Loger.Info_log.info("[ERROR]getClassList()方法执行错误 " , e);
		}
		if(null == listClass || "".equals(listClass)){
			Loger.Info_log.info("[ERROR]接口实现类获取失败");
		}
		
		ai.analyse(listClass);
	}
	
	@SuppressWarnings({ "rawtypes" })
	/**
	 * 此方法用于获取指定package中所有非内部累
	 * 
	 * @return list
	 * 			所有class文件类名
	 * */
	private List<Class> getClassList(){
		
		//获取接口实现类
		List<Class> list = getClasssFromPackage(packageName);//"guodu.net.analyse"
		String className = "";
		int size = list.size();
		for(int i = 0 ; i < size ; i++){
			className = list.get(i).getName();
			if(className.contains("$") || "".equals(className) || null == className){
				list.remove(i);
				size = size - 1;
				i--;
			}else{
				Loger.Info_log.info("[INFO]" + packageName + "包含分析类：" + className);
			}
		}
		return list;
	}
	
	
	/**
	 * 获得包下面的所有的class
	 * 
	 * @param pack
	 *            package完整名称
	 * @return List
	 * 			  包含所有class的实例
	 */
	@SuppressWarnings("rawtypes")
	private static List<Class> getClasssFromPackage(String pack) {
	  List<Class> clazzs = new ArrayList<Class>();

	  // 是否循环搜索子包
	  boolean recursive = true;

	  // 包名字
	  String packageName = pack;
	  // 包名对应的路径名称
	  String packageDirName = packageName.replace('.', '/');

	  Enumeration<URL> dirs;

	  try {
	    dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
	    while (dirs.hasMoreElements()) {
	      URL url = dirs.nextElement();

	      String protocol = url.getProtocol();
	      String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
	      if ("file".equals(protocol)) {
	        findClassInPackageByFile(packageName, filePath, recursive, clazzs);
	      } else if ("jar".equals(protocol)) {
	    	clazzs = getClasssFromJarFile(filePath.replace("file:", "").replace("!/" + packageDirName, "") , packageDirName);
	      }
	    }
	  } catch (Exception e) {
	    e.printStackTrace();
	  }

	  return clazzs;
	}

	
	
	/**
	 * 从jar文件中读取指定目录下面的所有的class文件
	 * 
	 * @param jarPaht
	 *            jar文件存放的位置
	 * @param filePaht
	 *            指定的文件目录
	 * @return 所有的的class的对象
	 */
	@SuppressWarnings("rawtypes")
	public static List<Class> getClasssFromJarFile(String jarPaht, String filePaht) {
	  List<Class> clazzs = new ArrayList<Class>();
	  JarFile jarFile = null;
	  try {
		jarFile = new JarFile(jarPaht);
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}


	  List<JarEntry> jarEntryList = new ArrayList<JarEntry>();

	  Enumeration<JarEntry> ee = jarFile.entries();
	  while (ee.hasMoreElements()) {
	    JarEntry entry = (JarEntry) ee.nextElement();
	    // 过滤我们出满足我们需求的东西
	    if (entry.getName().startsWith(filePaht) && entry.getName().endsWith(".class")) {
	      jarEntryList.add(entry);
	    }
	  }
	  for (JarEntry entry : jarEntryList) {
	    String className = entry.getName().replace('/', '.');
	    className = className.substring(0, className.length() - 6);

	    try {
	      clazzs.add(Thread.currentThread().getContextClassLoader().loadClass(className));
	    } catch (ClassNotFoundException e) {
	      e.printStackTrace();
	    }
	  }

	  return clazzs;
	}
	
	
	/**
	   * 通过流将jar中的一个文件的内容输出
	   * 
	   * @param jarPaht
	   *            jar文件存放的位置
	   * @param filePaht
	   *            指定的文件目录
	   */
	  public static void getStream(String jarPaht, String filePaht) {
	    JarFile jarFile = null;
	    try {
	      jarFile = new JarFile(jarPaht);
	    } catch (IOException e1) {
	      e1.printStackTrace();
	    }
	    Enumeration<JarEntry> ee = jarFile.entries();

	    List<JarEntry> jarEntryList = new ArrayList<JarEntry>();
	    while (ee.hasMoreElements()) {
	      JarEntry entry = (JarEntry) ee.nextElement();
	      // 过滤我们出满足我们需求的东西，这里的fileName是指向一个具体的文件的对象的完整包路径，比如com/mypackage/test.txt
	      if (entry.getName().startsWith(filePaht)) {
	        jarEntryList.add(entry);
	      }
	    }
	    try {
	      InputStream in = jarFile.getInputStream(jarEntryList.get(0));
	      BufferedReader br = new BufferedReader(new InputStreamReader(in));
	      String s = "";

	      while ((s = br.readLine()) != null) {
	        System.out.println(s);
	      }
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	  }
	
	
	/**
	 * 在package对应的路径下找到所有的class
	 * 
	 * @param packageName
	 *            package名称
	 * @param filePath
	 *            package对应的路径
	 * @param recursive
	 *            是否查找子package
	 * @param clazzs
	 *            找到class以后存放的集合
	 */
	private static void findClassInPackageByFile(String packageName, String filePath, final boolean recursive, @SuppressWarnings("rawtypes") List<Class> clazzs) {
	  File dir = new File(filePath);
	  if (!dir.exists() || !dir.isDirectory()) {
	    return;
	  }
	  // 在给定的目录下找到所有的文件，并且进行条件过滤
	  File[] dirFiles = dir.listFiles(new FileFilter() {

	    public boolean accept(File file) {
	      boolean acceptDir = recursive && file.isDirectory();// 接受dir目录
	      boolean acceptClass = file.getName().endsWith("class");// 接受class文件
	      return acceptDir || acceptClass;
	    }
	  });

	  for (File file : dirFiles) {
	    if (file.isDirectory()) {
	      findClassInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, clazzs);
	    } else {
	      String className = file.getName().substring(0, file.getName().length() - 6);
	      try {
	        clazzs.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className));
	      } catch (Exception e) {
	        e.printStackTrace();
	      }
	    }
	  }
	}

}

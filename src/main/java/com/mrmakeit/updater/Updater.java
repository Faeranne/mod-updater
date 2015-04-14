package com.mrmakeit.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@TransformerExclusions(value = {})
public class Updater implements IFMLLoadingPlugin, IFMLCallHook
{

	public static final String mcVersion = "[1.7.10]";
	public static final String version = "0.1";
	public static Path minecraftDir;
	
	public Updater() {
		if (minecraftDir != null)
			return;
		minecraftDir = ((File) FMLInjectionData.data()[6]).toPath();
		List<String> modpack = null;
		try {
			modpack = Files.readAllLines(minecraftDir.resolve("config").resolve("modpack.url"), Charset.defaultCharset());
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			System.out.println("no modpack.url found in the config dir, can't download modpack.");
			return;
		}
		URL website;
		try {
			website = new URL(modpack.get(0));
			Files.copy(website.openStream(), minecraftDir.resolve("config").resolve("modpack.pak"), StandardCopyOption.REPLACE_EXISTING);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("Url in modpack.url is malformed.  Make sure it follows http://example.com/modpack.pak");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Couldn't save modpack.pak.  This means I couldn't read/write to the config dir.");
		}
		List<String> lines = null;
		try {
			lines = Files.readAllLines(minecraftDir.resolve("config").resolve("modpack.pak"), Charset.defaultCharset());
			for (String mod : lines){
				String[] parts = mod.split(";");
				downloadMod(parts[0],parts[1],parts[2]);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("Couldn't load modpack.pak.");
		}

	}
	
	@Override
	public Void call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getASMTransformerClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModContainerClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSetupClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAccessTransformerClass() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void downloadMod(String name, String path, String hash){
		System.out.println("Mod "+name+" is needed.");
		URL website;
		FileInputStream fis;
		String md5 = "";
		try {
			fis = new FileInputStream(minecraftDir.resolve("mods").resolve(name).toFile());
			md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			fis.close();
		} catch (IOException e1) {
		}
		if(hash.equals(md5)){
			System.out.println("Matches required version");
			return;
		}else{
			System.out.println("Current version is out of date or doesn't exist, updating.");
			try {
				website = new URL(path);
				Files.copy(website.openStream(), minecraftDir.resolve("mods").resolve(name), StandardCopyOption.REPLACE_EXISTING);
			} catch (MalformedURLException e) {
				System.out.println("Couldn't read from url.  Something in modpack.pak is wrong.");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Couldn't save "+name+".  This means I couldn't write to the mods dir.");
			}
		}
	}
	
}
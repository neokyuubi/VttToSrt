import org.apache.commons.io.FilenameUtils;
import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

	private static void showMessage(String txt)
	{
		JFrame f = new JFrame();
		JOptionPane.showMessageDialog(f, txt, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
    public static void main(String[] args)
	{
		if (args.length < 1)
		{
			showMessage("Drag the folder that contains the vtt files or in cmd pass it as the first argument.");
			System.exit(0);
		}
		String home = args[0];
		String patternTxt = "(\\d{2}:\\d{2})(\\.)(\\d{3})";
		Pattern pattern = Pattern.compile(patternTxt);
		List<Path> files = null;
		try
		{
			files = Files.walk(Paths.get(home)).filter(e -> e.toFile().getAbsolutePath().endsWith(".vtt")).collect(Collectors.toList());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			showMessage(e.getMessage());
			System.exit(0);
		}
		
		if (files != null)
		{
			files.stream().parallel().forEach(file -> {
				try(Stream<String> stream = Files.lines(file))
				{
					long total = Files.lines(file).count();
					AtomicInteger indexEmpty = new AtomicInteger(0);
					AtomicInteger indexLine = new AtomicInteger(0);
					List<String> list = stream.filter(e->!e.startsWith("WEBVTT")).map(line ->
				   {
					   int indexL = indexLine.incrementAndGet();
					   if (line.isEmpty() && indexL < total)
					   {
						   int indexJ = indexEmpty.incrementAndGet();
						   if (indexL > 1)
						   {
							   line = "\n" + String.valueOf(indexJ);
						   }
						   else
						   {
							   line = String.valueOf(indexJ);
						   }
					   }
					   else if (!line.isEmpty() && indexL < total)
					   {
						   if (pattern.matcher(line).lookingAt())
						   {
							   line = line.replaceAll(patternTxt, "00:$1,$3");
						   }
					   }
					   return line;
				   }).collect(Collectors.toList());
					String content = String.join("\n",list);
					try(BufferedWriter br = new BufferedWriter(new FileWriter(FilenameUtils.removeExtension(file.toFile().getAbsolutePath()) + ".srt")))
					{
						br.write(content);
					}
					catch (IOException e)
					{
						e.printStackTrace();
						showMessage(e.getMessage());
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
					showMessage(e.getMessage());
				}
			});
		}
		showMessage("Finished");
		System.exit(0);
	}
}

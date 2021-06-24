package me.ialistannen.simplecodetester.execution.diana;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public class Rewriter extends Remapper {

  public byte[] remap(byte[] classBytes, ClassLoader classLoader) {
    ClassReader reader = new ClassReader(classBytes);
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
      @Override
      protected ClassLoader getClassLoader() {
        return classLoader;
      }
    };
    reader.accept(
        new ClassRemapper(writer, new Mapper()),
        ClassReader.EXPAND_FRAMES | ClassReader.SKIP_FRAMES
    );

    return writer.toByteArray();
  }

  private static class Mapper extends Remapper {

    @Override
    public String map(String internalName) {
      if (internalName.equals("java/util/Scanner")) {
        return "me/ialistannen/simplecodetester/execution/diana/TerminalScanner";
      }
      if (internalName.equals("java/io/BufferedReader")) {
        return "me/ialistannen/simplecodetester/execution/diana/TerminalBufferedReader";
      }
      if (internalName.equals("java/io/FileReader")) {
        return "me/ialistannen/simplecodetester/execution/diana/TerminalFileReader";
      }
      return super.map(internalName);
    }
  }
}

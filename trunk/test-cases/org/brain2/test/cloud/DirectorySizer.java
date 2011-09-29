package org.brain2.test.cloud;


public class DirectorySizer{ 
//extends RecursiveTask<Long> {

/*  private List<File> mFiles;
  private boolean mAllFiles = true;

  public DirectorySizer(List<File> files) {
    mFiles = files;
    for (File file : files) {
      if (file.isDirectory()) {
        mAllFiles = false;
      }
    }
  }

  protected Long compute() {
    if (mFiles.size() <=4 && mAllFiles) {
      return computeLocal();
    } else {
      return forkAndJoin();
    }
  }

  private Long computeLocal() {
    long length = 0;
    for (File file : mFiles) {
	  length += file.length();
    }
    return length;
  }

  private Long forkAndJoin() {
    List<File> dirsAndFiles = new ArrayList();
	for (File file : mFiles) {
      if (file.isFile()) {
        dirsAndFiles.add(file);
      } else {
        dirsAndFiles.addAll(Arrays.asList(file.listFiles()));
      }
    }
    int rightSize = dirsAndFiles.size() / 2;
    int leftSize = dirsAndFiles.size() - rightSize;
    List<File> leftList = dirsAndFiles.subList(0, leftSize);
    List<File> rightList= dirsAndFiles.subList(leftSize, leftSize+rightSize);
    DirectorySizer d1 = new DirectorySizer(leftList);
    d1.fork();
    DirectorySizer d2 = new DirectorySizer(rightList);
    return d2.compute() + d1.join();
  }

  public static void main(String[] args) throws Exception {
    List<File> files = Arrays.asList(new File(args[0]).listFiles());
    DirectorySizer sizer = new DirectorySizer(files);
    ForkJoinPool pool = new ForkJoinPool();
    Long size = pool.invoke(sizer);
    System.out.println(args[0] + " is " + size + " bytes ");
  }*/
}
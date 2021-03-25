<ol type="1">
  <li>Проект представлен в том состоянии, на котором снималось видео.</li> <br>

<li>Проект сделан с использованием Java IO и Java NIO.</li> <br>

<li>За основу взят проект, который демонстрировался преподавателем на первом занятии.</li> <br>


<li>Метод protected String downloadFile(String filename) в классе Client.java написан с использованием Java NIO.
Следующий кусок кода в начале метода убирает из названия файла, полученного из текстового поля Swing ,пробелы и знаки переноса строки, которые могут там остаться в случае редактирования пользователем указанного поля вручную:</li> <br>


```
filename.trim();
System.out.println((byte) filename.charAt(filename.length() - 1));
Pattern pattern = Pattern.compile("\n");
Matcher matcher = pattern.matcher(filename);
filename = matcher.replaceAll("");
```

<li>Программа использует два порта: 1235 для методов, написанных на I/O и 1237 для NIO. Метод protected String downloadFile(String filename) в классе Client.java вначале получает по 1235 порту информацию о количестве буферов, которые поступят в его адрес. Оно рассчитывается сервером исходя из объема файла в методе private long calculateQuantityOfBuffers(File oneFile, ByteBuffer oneBuffer). Далее буферы считываются в цикле for. С циклом while реализовать не смог, т. к.  в середине файла откуда-то прилетает -1 и он воспринимает это как конец потока.</li> <br>

<li>Сервер передает буферы в методе private void performDownload(), также в цикле for  по тем же причинам. В конце метода в консоль выводится количество переданных байтов. </li> <br>
</ol>
<p align="center">
<strong>Вот все, что пока сумел реализовать. Проект буду дорабатывать, чтобы было что потенциальному работодателю показать. </strong>
</p>

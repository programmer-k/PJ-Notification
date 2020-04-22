# PJ Notification

**PJ Notification** is open-source **unofficial** Android application.
PJ Noticifation is for people who wants to get push alarmed course notifications for YSCEC.

## From Motivation to Creation

For many years, there was no official Android application for YSCEC which pushes alarm for course notifications.
For me, it is uncomfortable to login and check whether there is a new course notifications everytime.
Also, I sometimes encountered the situation that everybody knows about the notice except for me because I did not check the notice frequently.
I always wanted to make a easier way to check the course notifications.
I thought Android application is the best option because it can push alarm so that I do not have to login and check manually.
However, I had only a little knowledge about the Android so I had no way to realize my thoughts.

In 1st semester, 2019, I applied for a leave of absence from university.
While I was talking with my friend, I was motivated by him to create an Android application.
At that time, I had enough time to do that, so I just started while worrying about fail.

Even though there was some obstacles, I finally created one and published it in 27th, May, 2019.

## Google Play Store

You can check the **PJ Notification** [here](https://play.google.com/store/apps/details?id=org.steinsapk.pjnotification).

## Future Work

Further work would be

1. Supporting other university that does not have official notice system.
2. Supporting multi-language(English, Japanese, Chinese).
3. Optimize crawling routine
4. Remove HtmlUnit library
5. Modify UI
6. Receive new YSCEC message
7. Other new features such as crawling notice for department site.

## Note

README.md is not fully documented yet.
I will update it gradually.

## PJ Notification Pros

1. PJ Notification is safe.
There is no server that mediates between the client and YSCEC web page.
Thus, all of the work is done by the client and no private information is passed to the developer's server.
This is important because YSCEC ID and password are not only used for logging in to YSCEC website, but also used for logging in to YONSEI portal and all the other things that are related to YONSEI university.
By acquiring the YSCEC ID and password, one can access all of the private data, such as courses that have been registered, their personal addresses, phone numbers, names, transcripts, course materials, their assignment submissions.
In the worst case, one can cancel the course registration, or register a leave of absence from school.
Although it is just an assumption, as long as the server manages users' ID and password, there always will be risk for hacking, misuse by the server maintainer.


2. PJ Notification is versatile.
At first, it only supports notice board notification which almost every course has and use.
However, I found out that all the new materials should be notified unless the user does not want to.
Therefore, now the PJ Notification crawls all of the all of the materials such as files, other boards, and assignment page.
Also, in the recent version, there was a new feature that users can forbids some courses or boards from notification.
I just erase the notification when it seems useless such as question notification, but someone said it is unconvenient to be notified for every single new writings in boards and materials.
Thus, user can set whatever they want by disabling some of the courses and boards and get notification only they want.

3. PJ Notification is open-source.
PJ Notification uses HtmlUnit library to login which is GPL licenses.
Thus, PJ Notification is also GPL licensed.
People who wants to improve this application could create a pull request or fork for their own version.

## PJ Notification Cons

1. PJ Notification consumes mobile resources and battery.
While PJ Notification is safe because there is no server to store personal data, it periodically runs on background to crawl new data.
Thus, this consumes battery and resources proportional to the periodic crawl cycle.
This is inherent problem of PJ Notification and should be minimized by optimizing the crawling operation.

2. PJ Notification is not UI-friendly.
I do have have profound knowledge about Anroid and how to make clean UI.
Thus, I tried to mimic other websites and apps that seems simple but good-looking to me.
For example, the login page of the PJ Notification was created by mimicing the Y-attend Android application.
At first I spent pretty much effort to the UI before releasing the app, and the result was the login page.
However, I started to concentrate on funtionality rather than UI because I took much time to become good-looking for a single page.
This problem should be improved someday, but sometimes I cannot decide which one is better to choose.

3. PJ Notification has some errors.
From the first release, there was a review that the application is crashing.
I did not know the reason because the application worked well for my cellphone.
However, it turns out that some of the Samsung smartphones crashes and I had a hard time testing it because I did not have Samsung smartphone which crashes on the app at that time.
There were many errors because of the HtmlUnit. I do not know why it has an error because it ran without any errors on desktop and foreground task, but not for the background task.
Since I made a shift from HtmlUnit to Jsoup, errors decreased.
Still, there will be some errors that I have not found yet.
I also encounter some errors because of network but I have not handled it yet.

## Why to This Design?

### Crawling Server Design
At first, I thought running a server for crawling is a good way to implement the PJ Notification.
This is because
1. I did not know about Android, so making a program running on server will reduce the work on the application and make the application simple.
2. Running on a background would be limited by the Android system.

However, this design was abandoned because one of my friends who helped me a lot for creating this application said
1. Running a server to crawl would need a security and encryption.
I had little knowledge about the security and encryption. Although I can learn and find some good stuffs on the Internet, I cannot guarantee to provide a completely safe security.
2. Although most of the websites only store the hash value for the password, the server to crawl needs a full password. This is because a normal website is checking whether the user is a valid user for its website, crawling server is accessing another web server to crawl. Even though we store the hash value, the server needs a plain password. It means that the server should store the data to decode and it is meaningless to store both encrypted data and keys together.
3. The YSCEC server might block the crawling server. To support many users, the crawling server should login with many different ids. The maintainer of the YSCEC server might find out and block.
Although on campus, there are many shared computers that students login with their own ids and print the course materials, the crawling server would login much more frequently and continuously to support many users. Also, while the shared computers on campus have IP with Yonsei University, the crawling server would probably not have one.
For me, I suspected it would rarely happens because I was doubting whether there was a maintainer of the server who cares about the access records. If the university paid much attention to server, why would they do not update the outdated webpage and create an application to push notification?

But for the reasons that I mentioned above, I created with no crawling server.
Although it is inconvenient to debug, because I do not have any accesses to the user personal ids, I think this design is good even though there is some drawbacks.

### Directly Accessing to the YSCEC DB
Instead of crawling, directly accessing the DB server would be much better way because crawling takes more time to crawl one by one.
However, I did not try this approach because I thought it was not feasible.
Although there is an e-mail address on the login page of YSCEC, I thought they would not give me a permission to access because I was just a student without any community and there was no results at that time to support my request.